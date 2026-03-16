package com.auth.config.oauth;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import com.auth.api.model.Principal;
import com.auth.api.model.User;
import com.auth.config.AuthProperties;
import com.auth.config.controller.RefreshCookieWriter;
import com.auth.config.jwt.AuthJwtProperties;
import com.auth.core.service.AuthService;
import com.auth.spi.PasswordVerifier;
import com.auth.spi.RefreshTokenStore;
import com.auth.spi.TokenService;
import com.auth.spi.UserFinder;

class OAuth2AuthenticationSuccessHandlerTest {

	@Test
	@DisplayName("OAuth2 로그인 성공 시 JWT 응답과 refresh 쿠키를 반환한다.")
	void onAuthenticationSuccess_ReturnsJwtResponse() throws Exception {
		AuthProperties authProperties = new AuthProperties();
		authProperties.setRefreshCookieName("refresh_token");
		authProperties.setRefreshCookieSecure(false);

		AuthJwtProperties jwtProperties = new AuthJwtProperties();
		jwtProperties.setRefreshSeconds(3600);

		RefreshCookieWriter refreshCookieWriter = new RefreshCookieWriter(authProperties, jwtProperties);
		AuthService authService = new AuthService(
			fakeUserFinder(),
			fakePasswordVerifier(),
			new FakeTokenService(),
			new InMemoryRefreshTokenStore(),
			Duration.ofHours(1),
			Clock.fixed(Instant.parse("2026-03-15T00:00:00Z"), ZoneOffset.UTC)
		);

		OAuth2AuthenticationSuccessHandler handler = new OAuth2AuthenticationSuccessHandler(
			identity -> new Principal("internal-user-1", List.of("USER")),
			authService,
			refreshCookieWriter
		);

		DefaultOAuth2User oauthUser = new DefaultOAuth2User(
			List.of(new SimpleGrantedAuthority("ROLE_USER")),
			Map.of("sub", "provider-user-1", "email", "oauth@example.com", "name", "OAuth User"),
			"sub"
		);
		OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(
			oauthUser,
			oauthUser.getAuthorities(),
			"google"
		);

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		handler.onAuthenticationSuccess(request, response, authentication);

		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(response.getContentType()).startsWith("application/json");
		assertThat(response.getContentAsString()).isEqualTo("{\"accessToken\":\"access-internal-user-1\"}");
		assertThat(response.getHeader("Set-Cookie")).contains("refresh_token=refresh-internal-user-1");
	}

	private UserFinder fakeUserFinder() {
		return username -> Optional.of(new User("user-1", username, "hashed", List.of("USER")));
	}

	private PasswordVerifier fakePasswordVerifier() {
		return (rawPassword, storedHash) -> true;
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

	private static final class InMemoryRefreshTokenStore implements RefreshTokenStore {
		@Override
		public void save(String userId, String refreshToken, Instant expiresAt) {
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
