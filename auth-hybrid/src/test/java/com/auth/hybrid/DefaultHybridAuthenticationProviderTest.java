package com.auth.hybrid;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.auth.api.exception.AuthException;
import com.auth.api.exception.AuthFailureReason;
import com.auth.api.authentication.AuthenticationSource;
import com.auth.api.model.Principal;
import com.auth.hybrid.strategy.MatchingPrincipalConflictResolver;
import com.auth.hybrid.strategy.SourceFirstHybridResolutionStrategy;
import com.auth.spi.TokenService;
import com.auth.session.SessionAuthenticationProvider;

class DefaultHybridAuthenticationProviderTest {

	private final Principal jwtPrincipal = new Principal("jwt-user", List.of("JWT"));
	private final Principal sessionPrincipal = new Principal("session-user", List.of("SESSION"));

	private final TokenService tokenService = new FakeTokenService();
	private final SessionAuthenticationProvider sessionProvider = sessionId ->
		"session-ok".equals(sessionId) ? Optional.of(sessionPrincipal) : Optional.empty();

	private final HybridAuthenticationProvider provider = new DefaultHybridAuthenticationProvider(tokenService, sessionProvider);

	@Test
	@DisplayName("접근 토큰이 유효하면 세션보다 우선해서 반환한다")
	void prefersJwtTokenWhenAvailable() {
		HybridAuthenticationContext context = HybridAuthenticationContext.of("valid-token", "session-ok");

		assertThat(provider.authenticate(context)).contains(jwtPrincipal);
	}

	@Test
	@DisplayName("JWT가 없으면 세션 인증으로 대체한다")
	void fallsBackToSessionWhenJwtMissing() {
		HybridAuthenticationContext context = HybridAuthenticationContext.of(null, "session-ok");

		assertThat(provider.authenticate(context)).contains(sessionPrincipal);
	}

	@Test
	@DisplayName("JWT가 불량이면 세션으로 폴백한다")
	void invalidJwtFallsBackToSession() {
		HybridAuthenticationContext context = HybridAuthenticationContext.of("invalid", "session-ok");

		assertThat(provider.authenticate(context)).contains(sessionPrincipal);
	}

	@Test
	@DisplayName("전략을 바꾸면 세션을 JWT보다 우선할 수 있다")
	void supportsSessionFirstStrategy() {
		HybridAuthenticationProvider sessionFirstProvider = new DefaultHybridAuthenticationProvider(
			tokenService,
			sessionProvider,
			SourceFirstHybridResolutionStrategy.sessionThenJwt(),
			(preferredSource, preferred, otherSource, other) -> preferred
		);

		HybridAuthenticationContext context = HybridAuthenticationContext.of("valid-token", "session-ok");

		assertThat(sessionFirstProvider.authenticate(context)).contains(sessionPrincipal);
	}

	@Test
	@DisplayName("충돌 resolver는 서로 다른 principal을 거부할 수 있다")
	void supportsConflictResolver() {
		HybridAuthenticationProvider strictProvider = new DefaultHybridAuthenticationProvider(
			tokenService,
			sessionProvider,
			new SourceFirstHybridResolutionStrategy(List.of(AuthenticationSource.JWT, AuthenticationSource.SESSION)),
			new MatchingPrincipalConflictResolver()
		);

		HybridAuthenticationContext context = HybridAuthenticationContext.of("valid-token", "session-ok");

		org.assertj.core.api.Assertions.assertThatThrownBy(() -> strictProvider.authenticate(context))
			.isInstanceOf(AuthException.class);
	}

	private final class FakeTokenService implements TokenService {
		@Override
		public String issueAccessToken(Principal principal) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String issueRefreshToken(Principal principal) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Principal verifyAccessToken(String token) {
			if ("valid-token".equals(token)) {
				return jwtPrincipal;
			}
			throw new AuthException(AuthFailureReason.INVALID_TOKEN, "invalid access token");
		}

		@Override
		public Principal verifyRefreshToken(String token) {
			throw new UnsupportedOperationException();
		}
	}
}
