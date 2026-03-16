package com.auth.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.auth.api.model.Principal;
import com.auth.api.model.Tokens;
import com.auth.api.model.User;
import com.auth.spi.PasswordVerifier;
import com.auth.spi.RefreshTokenStore;
import com.auth.spi.TokenService;
import com.auth.spi.UserFinder;

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

		Principal principal = new Principal("oauth-user", List.of("USER"));

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
		UserFinder userFinder = username -> Optional.of(new User("user-1", username, "hashed", List.of("ADMIN")));
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
			throw new UnsupportedOperationException();
		}
	}

	private static final class FakeRefreshTokenStore implements RefreshTokenStore {
		private String savedUserId;
		private String savedRefreshToken;
		private Instant savedExpiresAt;

		@Override
		public void save(String userId, String refreshToken, Instant expiresAt) {
			this.savedUserId = userId;
			this.savedRefreshToken = refreshToken;
			this.savedExpiresAt = expiresAt;
		}

		@Override
		public boolean exists(String userId, String refreshToken) {
			return false;
		}

		@Override
		public void revoke(String userId, String refreshToken) {
		}
	}
}
