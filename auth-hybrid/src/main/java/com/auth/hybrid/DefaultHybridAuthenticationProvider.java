package com.auth.hybrid;

import com.auth.api.authentication.AuthenticationSource;
import com.auth.api.model.Principal;
import com.auth.hybrid.strategy.HybridConflictResolver;
import com.auth.hybrid.strategy.HybridResolutionStrategy;
import com.auth.hybrid.strategy.PreferFirstConflictResolver;
import com.auth.hybrid.strategy.SourceFirstHybridResolutionStrategy;
import com.auth.spi.TokenService;
import com.auth.session.SessionAuthenticationProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Objects;

/** Hybrid provider with generic source priority and conflict resolution. */
public final class DefaultHybridAuthenticationProvider implements HybridAuthenticationProvider {

    private final TokenService tokenService;
    private final SessionAuthenticationProvider sessionAuthenticationProvider;
    private final HybridResolutionStrategy resolutionStrategy;
    private final HybridConflictResolver conflictResolver;

	/**
	 * 생성자
	 * @param tokenService
	 * @param sessionAuthenticationProvider
	 */
    public DefaultHybridAuthenticationProvider(TokenService tokenService, SessionAuthenticationProvider sessionAuthenticationProvider) {
        this(tokenService, sessionAuthenticationProvider, SourceFirstHybridResolutionStrategy.jwtThenSession(), new PreferFirstConflictResolver());
    }

    public DefaultHybridAuthenticationProvider(
        TokenService tokenService,
        SessionAuthenticationProvider sessionAuthenticationProvider,
        HybridResolutionStrategy resolutionStrategy,
        HybridConflictResolver conflictResolver
    ) {
        this.tokenService = Objects.requireNonNull(tokenService, "tokenService");
        this.sessionAuthenticationProvider = Objects.requireNonNull(sessionAuthenticationProvider, "sessionAuthenticationProvider");
        this.resolutionStrategy = Objects.requireNonNull(resolutionStrategy, "resolutionStrategy");
        this.conflictResolver = Objects.requireNonNull(conflictResolver, "conflictResolver");
    }

    @Override
    public Optional<Principal> authenticate(HybridAuthenticationContext context) {
        List<HybridAuthenticationAttempt> attempts = new ArrayList<>();
        for (AuthenticationSource source : resolutionStrategy.sourceOrder()) {
            attempts.add(authenticateSource(source, context));
        }

        HybridAuthenticationAttempt selected = null;
        for (HybridAuthenticationAttempt attempt : attempts) {
            if (attempt.principal().isEmpty()) continue;
            if (selected == null) {
                selected = attempt;
                continue;
            }
            Principal resolved = conflictResolver.resolve(
                selected.source(),
                selected.principal().get(),
                attempt.source(),
                attempt.principal().get()
            );
            selected = new HybridAuthenticationAttempt(selected.source(), Optional.of(resolved));
        }
        return selected == null ? Optional.empty() : selected.principal();
    }

    private HybridAuthenticationAttempt authenticateSource(AuthenticationSource source, HybridAuthenticationContext context) {
        return switch (source) {
            case JWT -> new HybridAuthenticationAttempt(AuthenticationSource.JWT, context.accessToken().flatMap(this::verifyToken));
            case SESSION -> new HybridAuthenticationAttempt(AuthenticationSource.SESSION, context.sessionId().flatMap(sessionAuthenticationProvider::authenticate));
            default -> HybridAuthenticationAttempt.empty(source);
        };
    }

    private Optional<Principal> verifyToken(String token) {
        try {
            return Optional.of(tokenService.verifyAccessToken(token));
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }
}
