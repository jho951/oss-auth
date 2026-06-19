package com.auth.core.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import com.auth.core.api.exception.AuthException;
import com.auth.core.api.exception.AuthFailureReason;
import com.auth.core.api.model.Principal;
import com.auth.core.api.model.Tokens;
import com.auth.core.api.model.User;

import com.auth.core.spi.PasswordVerifier;
import com.auth.core.spi.RefreshTokenStore;
import com.auth.core.spi.TokenService;
import com.auth.core.spi.UserFinder;
import com.auth.core.spi.UserStatusChecker;
import com.auth.core.utils.MoreObjects;
import com.auth.core.utils.Strings;

/** OSS auth의 순수 인증 유즈케이스입니다. */
public final class AuthService {

	/** 사용자 정보를 조회하기 위한 인터페이스 (유저 정보) */
	private final UserFinder userFinder;
	/** 비밀번호 암호화 및 일치 여부 확인 인터페이스 */
	private final PasswordVerifier passwordVerifier;
	/** 사용자의 인증 가능 상태를 판정하는 인터페이스 */
	private final UserStatusChecker userStatusChecker;
	/** 토큰(JWT 등) 생성 및 검증 인터페이스 */
	private final TokenService tokenService;
	/** 리프레시 토큰의 영속성 관리를 위한 저장소 인터페이스 */
	private final RefreshTokenStore refreshTokenStore;
	/** 리프레시 토큰의 유효 기간 (null 또는 0 이하일 경우 기본값 14일) */
	private final Duration refreshTtl;
	/** 시간 계산을 위한 클럭 (테스트 시 모킹 가능) */
	private final Clock clock;

	public AuthService(
		UserFinder userFinder,
		PasswordVerifier passwordVerifier,
		TokenService tokenService,
		RefreshTokenStore refreshTokenStore,
		Duration refreshTtl
	) {
		this(userFinder, passwordVerifier, UserStatusChecker.allowAll(), tokenService, refreshTokenStore, refreshTtl, Clock.systemUTC());
	}

	public AuthService(
		UserFinder userFinder,
		PasswordVerifier passwordVerifier,
		TokenService tokenService,
		RefreshTokenStore refreshTokenStore,
		Duration refreshTtl,
		Clock clock
	) {
		this(userFinder, passwordVerifier, UserStatusChecker.allowAll(), tokenService, refreshTokenStore, refreshTtl, clock);
	}

	public AuthService(
		UserFinder userFinder,
		PasswordVerifier passwordVerifier,
		UserStatusChecker userStatusChecker,
		TokenService tokenService,
		RefreshTokenStore refreshTokenStore,
		Duration refreshTtl
	) {
		this(userFinder, passwordVerifier, userStatusChecker, tokenService, refreshTokenStore, refreshTtl, Clock.systemUTC());
	}

	public AuthService(
		UserFinder userFinder,
		PasswordVerifier passwordVerifier,
		UserStatusChecker userStatusChecker,
		TokenService tokenService,
		RefreshTokenStore refreshTokenStore,
		Duration refreshTtl,
		Clock clock
	) {
        this.userFinder = Strings.requireNonNull(userFinder, "userFinder");
        this.passwordVerifier = Strings.requireNonNull(passwordVerifier, "passwordVerifier");
        this.userStatusChecker = Strings.requireNonNull(userStatusChecker, "userStatusChecker");
        this.tokenService = Strings.requireNonNull(tokenService, "tokenService");
        this.refreshTokenStore = Strings.requireNonNull(refreshTokenStore, "refreshTokenStore");
        this.refreshTtl =
            (refreshTtl == null || refreshTtl.isNegative() || refreshTtl.isZero()) ? Duration.ofDays(14) : refreshTtl;
        this.clock = MoreObjects.defaultIfNull(clock, Clock.systemUTC());
    }

	/**
	 * 사용자의 자격 증명을 확인하고 새로운 토큰 세트를 발급합니다.
	 * @param username 사용자 계정명
	 * @param password 평문 비밀번호
	 * @return 발급된 Access Token과 Refresh Token 쌍
	 * @throws AuthException 유저를 찾을 수 없거나 비밀번호가 틀린 경우 (USER_NOT_FOUND, INVALID_CREDENTIALS)
	 */
	public Tokens login(String username, String password) {
		if (Strings.isBlank(username)) throw new AuthException(AuthFailureReason.INVALID_INPUT, "username must not be blank");
		if (Strings.isBlank(password)) throw new AuthException(AuthFailureReason.INVALID_INPUT, "password must not be blank");

		User user = userFinder.findByUsername(username).orElseThrow(() -> new AuthException(AuthFailureReason.USER_NOT_FOUND, "user not found"));

		AuthFailureReason statusFailureReason = userStatusChecker.check(user).orElse(null);
		if (statusFailureReason != null) {
			throw new AuthException(statusFailureReason, userStatusMessage(statusFailureReason));
		}

		boolean ok = passwordVerifier.matches(password, user.getPasswordHash());

		if (!ok) throw new AuthException(AuthFailureReason.INVALID_CREDENTIALS, "invalid credentials");

		return login(new Principal(user.getUserId(), user.getAuthorities()));
	}

	private static String userStatusMessage(AuthFailureReason reason) {
		switch (reason) {
			case USER_DISABLED:
				return "user disabled";
			case USER_LOCKED:
				return "user locked";
			case USER_EXPIRED:
				return "user expired";
			case CREDENTIALS_EXPIRED:
				return "credentials expired";
			default:
				return reason.name().toLowerCase();
		}
	}

	/**
	 * 이미 외부에서 인증이 끝난 사용자를 기준으로 새로운 토큰 세트를 발급합니다.
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
	 * 리프레시 토큰을 사용하여 새로운 토큰 쌍을 재발급합니다.
	 * @param refreshToken 유효한 리프레시 토큰
	 * @return 새로 발급된 Access Token과 Refresh Token 쌍
	 * @throws AuthException 토큰이 변조되었거나, 만료되었거나, 서버 저장소에 존재하지 않는 경우 (INVALID_TOKEN, REVOKED_TOKEN)
	 */
	public Tokens refresh(String refreshToken) {
		if (Strings.isBlank(refreshToken)) throw new AuthException(AuthFailureReason.INVALID_INPUT, "refreshToken must not be blank");

		Principal principal;
		try {
			principal = tokenService.verifyRefreshToken(refreshToken);
		} catch (RuntimeException e) {
			throw new AuthException(AuthFailureReason.INVALID_TOKEN, "invalid refresh token", e);
		}

		boolean exists = refreshTokenStore.exists(principal.getUserId(), refreshToken);
		if (!exists) throw new AuthException(AuthFailureReason.REVOKED_TOKEN, "refresh token revoked");

		refreshTokenStore.revoke(principal.getUserId(), refreshToken);

		String newAccess = tokenService.issueAccessToken(principal);
		String newRefresh = tokenService.issueRefreshToken(principal);

		Instant expiresAt = Instant.now(clock).plus(refreshTtl);
		refreshTokenStore.save(principal.getUserId(), newRefresh, expiresAt);

		return new Tokens(newAccess, newRefresh);
	}

	/**
	 * 사용자를 로그아웃 처리하고 리프레시 토큰을 무효화합니다.
	 * @param refreshToken 무효화할 리프레시 토큰
	 * @throws AuthException 토큰 형식이 잘못되었을 경우
	 */
	public void logout(String refreshToken) {
		if (Strings.isBlank(refreshToken)) throw new AuthException(AuthFailureReason.INVALID_INPUT, "refreshToken must not be blank");

		Principal principal;
		try {
			principal = tokenService.verifyRefreshToken(refreshToken);
		} catch (RuntimeException e) {
			throw new AuthException(AuthFailureReason.INVALID_TOKEN, "invalid refresh token", e);
		}

		refreshTokenStore.revoke(principal.getUserId(), refreshToken);
	}


}
