package com.auth.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
import com.auth.core.spi.token.RefreshTokenRotation;

class AuthServiceTest {

	@Test
	@DisplayName("이미 인증된 Principal로 access/refresh 토큰을 발급한다.")
	void loginWithPrincipal_IssuesTokens() {
		FakeRefreshTokenStore refreshTokenStore = new FakeRefreshTokenStore();
		AuthService authService = new AuthService(
			username -> Optional.empty(),
			(rawPassword, storedHash) -> false,
			new FakeTokenService(),
			refreshTokenStore,
			Duration.ofDays(14),
			Clock.fixed(Instant.parse("2026-03-15T00:00:00Z"), ZoneOffset.UTC)
		);

		Principal principal = new Principal("oauth-user", com.auth.core.utils.CollectionUtils.listOf("USER"));

		Tokens tokens = authService.login(principal);

		assertThat(tokens.getAccessToken()).isEqualTo("access-oauth-user");
		assertThat(tokens.getRefreshToken()).isEqualTo("refresh-oauth-user");
		assertThat(refreshTokenStore.savedUserId).isEqualTo("oauth-user");
		assertThat(refreshTokenStore.savedRefreshToken).isEqualTo("refresh-oauth-user");
		assertThat(refreshTokenStore.savedExpiresAt).isEqualTo(Instant.parse("2026-03-29T00:00:00Z"));
	}

	@Test
	@DisplayName("username/password 로그인은 기존과 동일하게 토큰을 발급한다.")
	void loginWithCredentials_DelegatesToPrincipalFlow() {
		FakeRefreshTokenStore refreshTokenStore = new FakeRefreshTokenStore();
		UserFinder userFinder = username -> Optional.of(new User("user-1", username, "hashed", com.auth.core.utils.CollectionUtils.listOf("ADMIN")));
		PasswordVerifier passwordVerifier = (rawPassword, storedHash) -> true;
		AuthService authService = new AuthService(
			userFinder,
			passwordVerifier,
			new FakeTokenService(),
			refreshTokenStore,
			Duration.ofDays(14),
			Clock.fixed(Instant.parse("2026-03-15T00:00:00Z"), ZoneOffset.UTC)
		);

		Tokens tokens = authService.login("admin", "plain-password");

		assertThat(tokens.getAccessToken()).isEqualTo("access-user-1");
		assertThat(tokens.getRefreshToken()).isEqualTo("refresh-user-1");
		assertThat(refreshTokenStore.savedUserId).isEqualTo("user-1");
	}

	@Test
	@DisplayName("username/password 로그인에서 상태 검사기가 사용자를 거부하면 해당 사유로 예외가 발생한다.")
	void loginWithCredentials_StatusCheckerRejectsUser() {
		UserFinder userFinder = username -> Optional.of(new User("user-1", username, "hashed", com.auth.core.utils.CollectionUtils.listOf("ADMIN")));
		boolean[] passwordVerifierCalled = {false};
		PasswordVerifier passwordVerifier = (rawPassword, storedHash) -> {
			passwordVerifierCalled[0] = true;
			return true;
		};
		UserStatusChecker userStatusChecker = user -> Optional.of(AuthFailureReason.USER_DISABLED);
		AuthService authService = new AuthService(
			userFinder,
			passwordVerifier,
			userStatusChecker,
			new FakeTokenService(),
			new FakeRefreshTokenStore(),
			Duration.ofDays(14),
			Clock.systemUTC()
		);

		assertThatThrownBy(() -> authService.login("admin", "plain-password"))
			.isInstanceOf(AuthException.class)
			.extracting(error -> ((AuthException) error).getReason())
			.isEqualTo(AuthFailureReason.USER_DISABLED);
		assertThat(passwordVerifierCalled[0]).isFalse();
	}

	@Test
	@DisplayName("OAuth 경로에서 Principal이 없으면 예외가 발생한다.")
	void loginWithPrincipal_NullPrincipal_Throws() {
		AuthService authService = new AuthService(
			username -> Optional.empty(),
			(rawPassword, storedHash) -> false,
			new FakeTokenService(),
			new FakeRefreshTokenStore(),
			Duration.ofDays(14),
			Clock.systemUTC()
		);

		assertThatThrownBy(() -> authService.login((Principal) null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("principal");
	}

	@Test
	@DisplayName("refresh는 rotation strategy가 발급한 다음 refresh token을 저장한다.")
	void refresh_UsesRotationStrategy() {
		FakeRefreshTokenStore refreshTokenStore = new FakeRefreshTokenStore();
		refreshTokenStore.existsResult = true;
		Instant nextExpiresAt = Instant.parse("2026-04-01T00:00:00Z");
		AuthService authService = new AuthService(
			username -> Optional.empty(),
			(rawPassword, storedHash) -> false,
			UserStatusChecker.allowAll(),
			new FakeTokenService(),
			refreshTokenStore,
			Duration.ofDays(14),
			Clock.fixed(Instant.parse("2026-03-15T00:00:00Z"), ZoneOffset.UTC),
			(principal, currentRefreshToken) -> new RefreshTokenRotation(
				currentRefreshToken,
				"rotated-refresh-token",
				nextExpiresAt
			)
		);

		Tokens tokens = authService.refresh("valid-refresh-user-1");

		assertThat(tokens.getAccessToken()).isEqualTo("access-user-1");
		assertThat(tokens.getRefreshToken()).isEqualTo("rotated-refresh-token");
		assertThat(refreshTokenStore.revokedUserId).isEqualTo("user-1");
		assertThat(refreshTokenStore.revokedRefreshToken).isEqualTo("valid-refresh-user-1");
		assertThat(refreshTokenStore.savedUserId).isEqualTo("user-1");
		assertThat(refreshTokenStore.savedRefreshToken).isEqualTo("rotated-refresh-token");
		assertThat(refreshTokenStore.savedExpiresAt).isEqualTo(nextExpiresAt);
	}

	@Test
	@DisplayName("기본 refresh rotation strategy는 기존 TTL 기준으로 다음 refresh token을 저장한다.")
	void refresh_DefaultRotationStrategyPreservesExistingBehavior() {
		FakeRefreshTokenStore refreshTokenStore = new FakeRefreshTokenStore();
		refreshTokenStore.existsResult = true;
		AuthService authService = new AuthService(
			username -> Optional.empty(),
			(rawPassword, storedHash) -> false,
			new FakeTokenService(),
			refreshTokenStore,
			Duration.ofDays(14),
			Clock.fixed(Instant.parse("2026-03-15T00:00:00Z"), ZoneOffset.UTC)
		);

		Tokens tokens = authService.refresh("valid-refresh-user-1");

		assertThat(tokens.getAccessToken()).isEqualTo("access-user-1");
		assertThat(tokens.getRefreshToken()).isEqualTo("refresh-user-1");
		assertThat(refreshTokenStore.revokedRefreshToken).isEqualTo("valid-refresh-user-1");
		assertThat(refreshTokenStore.savedRefreshToken).isEqualTo("refresh-user-1");
		assertThat(refreshTokenStore.savedExpiresAt).isEqualTo(Instant.parse("2026-03-29T00:00:00Z"));
	}

	private static final class FakeTokenService implements TokenService {
		@Override
		public String issueAccessToken(Principal principal) {
			return "access-" + principal.getUserId();
		}

		@Override
		public String issueRefreshToken(Principal principal) {
			return "refresh-" + principal.getUserId();
		}

		@Override
		public Principal verifyAccessToken(String token) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Principal verifyRefreshToken(String token) {
			if (token != null && token.startsWith("valid-refresh-")) {
				return new Principal(token.substring("valid-refresh-".length()));
			}
			throw new UnsupportedOperationException();
		}
	}

	private static final class FakeRefreshTokenStore implements RefreshTokenStore {
		private String savedUserId;
		private String savedRefreshToken;
		private Instant savedExpiresAt;
		private boolean existsResult;
		private String revokedUserId;
		private String revokedRefreshToken;

		@Override
		public void save(String userId, String refreshToken, Instant expiresAt) {
			this.savedUserId = userId;
			this.savedRefreshToken = refreshToken;
			this.savedExpiresAt = expiresAt;
		}

		@Override
		public boolean exists(String userId, String refreshToken) {
			return existsResult;
		}

		@Override
		public void revoke(String userId, String refreshToken) {
			this.revokedUserId = userId;
			this.revokedRefreshToken = refreshToken;
		}
	}
}
