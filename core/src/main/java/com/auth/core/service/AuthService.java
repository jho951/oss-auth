package com.auth.core.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import com.auth.api.exception.AuthException;
import com.auth.api.exception.ErrorCode;
import com.auth.api.model.Principal;
import com.auth.api.model.Tokens;
import com.auth.api.model.User;

import com.auth.spi.PasswordVerifier;
import com.auth.spi.RefreshTokenStore;
import com.auth.spi.TokenService;
import com.auth.spi.UserFinder;
import com.auth.common.utils.Strings;

/**
 * <h2>인증 유즈케이스 서비스 (AuthService)</h2>
 * <p>인증 모듈의 비즈니스 로직입니다.</p>
 * <p><b>순수 Java 로직</b>으로 작성해 특정 프레임워크나 HTTP 프로토콜, 특정 데이터베이스 기술에 종속되지 않습니다.</p>
 * <p>의존성 역전 원칙(DIP)에 따라 인터페이스(SPI)에 의존하며, 실제 구현체는 실행 시점에 주입받아 동작합니다.</p>
 * <p><b>주요 역할:</b></p>
 * <ul>
 * <li>사용자 자격 증명(Username/Password) 확인</li>
 * <li>액세스 및 리프레시 토큰 통합 발급 (Login)</li>
 * <li>리프레시 토큰 회전 정책을 통한 토큰 재발급 (Refresh)</li>
 * <li>서버 측 세션 무효화 (Logout)</li>
 * </ul>
 */
public final class AuthService {

	private final UserFinder userFinder;
	private final PasswordVerifier passwordVerifier;
	private final TokenService tokenService;
	private final RefreshTokenStore refreshTokenStore;
	private final Duration refreshTtl;
	private final Clock clock;

	/**
	 * AuthService를 생성합니다.
	 * @param userFinder 사용자 정보를 조회하기 위한 인터페이스
	 * @param passwordVerifier 비밀번호 암호화 및 일치 여부 확인 인터페이스
	 * @param tokenService 토큰(JWT 등) 생성 및 검증 인터페이스
	 * @param refreshTokenStore 리프레시 토큰의 영속성 관리를 위한 저장소 인터페이스
	 * @param refreshTtl 리프레시 토큰의 유효 기간 (null 또는 0 이하일 경우 기본값 14일)
	 * @param clock 시간 계산을 위한 클럭 (테스트 시 모킹 가능)
	 * @throws IllegalArgumentException 필수 인자가 null일 경우 발생
	 */
	public AuthService(
		UserFinder userFinder,
		PasswordVerifier passwordVerifier,
		TokenService tokenService,
		RefreshTokenStore refreshTokenStore,
		Duration refreshTtl,
		Clock clock
	) {
		this.userFinder = Strings.requireNonNull(userFinder, "userFinder");
		this.passwordVerifier = Strings.requireNonNull(passwordVerifier, "passwordVerifier");
		this.tokenService = Strings.requireNonNull(tokenService, "tokenService");
		this.refreshTokenStore = Strings.requireNonNull(refreshTokenStore, "refreshTokenStore");
		this.refreshTtl = (refreshTtl == null || refreshTtl.isNegative() || refreshTtl.isZero()) ? Duration.ofDays(14) : refreshTtl;
		this.clock = (clock == null) ? Clock.systemUTC() : clock;
	}

	/** 시스템 기본 시계(UTC)를 사용하여 서비스를 생성합니다 */
	public AuthService(
		UserFinder userFinder,
		PasswordVerifier passwordVerifier,
		TokenService tokenService,
		RefreshTokenStore refreshTokenStore,
		Duration refreshTtl
	) {
		this(userFinder, passwordVerifier, tokenService, refreshTokenStore, refreshTtl, Clock.systemUTC());
	}

	/**
	 * 사용자의 자격 증명을 확인하고 새로운 토큰 세트를 발급합니다.
	 * <ol>
	 * <li>사용자 존재 여부 및 비밀번호 일치 여부 확인</li>
	 * <li>Principal 객체 생성 (UserId, Roles 포함)</li>
	 * <li>Access/Refresh 토큰 쌍 생성</li>
	 * <li>추후 검증을 위해 Refresh 토큰을 저장소에 기록</li>
	 * </ol>
	 * * @param username 사용자 계정명
	 * @param password 평문 비밀번호
	 * @return 발급된 Access Token과 Refresh Token 쌍
	 * @throws AuthException 유저를 찾을 수 없거나 비밀번호가 틀린 경우 (USER_NOT_FOUND, INVALID_CREDENTIALS)
	 */
	public Tokens login(String username, String password) {
		if (Strings.isBlank(username)) throw new AuthException(ErrorCode.BLANK_USER_ID, "username must not be blank");
		if (Strings.isBlank(password)) throw new AuthException(ErrorCode.BLANK_PASSWORD, "password must not be blank");

		User user = userFinder.findByUsername(username).orElseThrow(() -> new AuthException(ErrorCode.USER_NOT_FOUND, "user not found"));

		boolean ok = passwordVerifier.matches(password, user.getPasswordHash());

		if (!ok) {throw new AuthException(ErrorCode.INVALID_CREDENTIALS, "invalid credentials");}

		return login(new Principal(user.getUserId(), user.getRoles()));
	}

	/**
	 * 이미 외부에서 인증이 끝난 사용자를 기준으로 새로운 토큰 세트를 발급합니다.
	 * <p>OAuth2/OIDC 로그인처럼 비밀번호 검증을 이 모듈 밖에서 수행한 경우에 사용합니다.</p>
	 * @param principal 내부 사용자 식별자와 권한을 담은 Principal
	 * @return 발급된 Access Token과 Refresh Token 쌍
	 */
	public Tokens login(Principal principal) {
		Principal authenticatedPrincipal = Strings.requireNonNull(principal, "principal");

		String access = tokenService.issueAccessToken(authenticatedPrincipal);
		String refresh = tokenService.issueRefreshToken(authenticatedPrincipal);

		Instant expiresAt = Instant.now(clock).plus(refreshTtl);

		refreshTokenStore.save(authenticatedPrincipal.getUserId(), refresh, expiresAt);

		return new Tokens(access, refresh);
	}

	/**
	 * 리프레시 토큰을 사용하여 새로운 토큰 쌍을 재발급합니다. (Token Rotation)
	 * * <p>보안 강화를 위해 <b>Refresh Token Rotation</b> 정책을 사용합니다.
	 * 새로운 토큰이 발급되면 기존의 리프레시 토큰은 즉시 폐기됩니다.</p>
	 * * @param refreshToken 유효한 리프레시 토큰
	 * @return 새로 발급된 Access Token과 Refresh Token 쌍
	 * @throws AuthException 토큰이 변조되었거나, 만료되었거나, 서버 저장소에 존재하지 않는 경우 (INVALID_TOKEN, TOKEN_REVOKED)
	 */
	public Tokens refresh(String refreshToken) {
		if (Strings.isBlank(refreshToken)) {
			throw new AuthException(ErrorCode.INVALID_REQUEST, "refreshToken must not be blank");
		}

		Principal principal;
		try {
			principal = tokenService.verifyRefreshToken(refreshToken);
		} catch (RuntimeException e) {
			throw new AuthException(ErrorCode.INVALID_TOKEN, "invalid refresh token", e);
		}

		boolean exists = refreshTokenStore.exists(principal.getUserId(), refreshToken);
		if (!exists) {
			throw new AuthException(ErrorCode.TOKEN_REVOKED, "refresh token revoked");
		}

		// 기존 토큰 무효화 (Rotation 실행)
		refreshTokenStore.revoke(principal.getUserId(), refreshToken);

		String newAccess = tokenService.issueAccessToken(principal);
		String newRefresh = tokenService.issueRefreshToken(principal);

		Instant expiresAt = Instant.now(clock).plus(refreshTtl);
		refreshTokenStore.save(principal.getUserId(), newRefresh, expiresAt);

		return new Tokens(newAccess, newRefresh);
	}

	/**
	 * 사용자를 로그아웃 처리하고 리프레시 토큰을 무효화합니다.
	 * * <p>이 메서드는 서버에 저장된 Refresh 토큰을 삭제하여 더 이상 토큰 갱신이 불가능하게 만듭니다.
	 * 이미 발급된 Access 토큰은 만료 전까지 유효할 수 있습니다.</p>
	 * * @param refreshToken 무효화할 리프레시 토큰
	 * @throws AuthException 토큰 형식이 잘못되었을 경우
	 */
	public void logout(String refreshToken) {
		if (Strings.isBlank(refreshToken)) {
			throw new AuthException(ErrorCode.BLANK_REFRESH_TOKEN, "refreshToken must not be blank");
		}

		Principal principal;
		try {
			principal = tokenService.verifyRefreshToken(refreshToken);
		} catch (RuntimeException e) {
			throw new AuthException(ErrorCode.INVALID_TOKEN, "invalid refresh token", e);
		}

		refreshTokenStore.revoke(principal.getUserId(), refreshToken);
	}


}
