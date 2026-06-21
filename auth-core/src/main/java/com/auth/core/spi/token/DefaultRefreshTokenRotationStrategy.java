package com.auth.core.spi.token;

import com.auth.core.api.model.Principal;
import com.auth.core.spi.TokenService;
import com.auth.core.utils.Strings;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/** 기본 refresh token rotation 전략입니다. */
public final class DefaultRefreshTokenRotationStrategy implements RefreshTokenRotationStrategy {

	private static final Duration DEFAULT_REFRESH_TTL = Duration.ofDays(14);

	private final TokenService tokenService;
	private final Duration refreshTtl;
	private final Clock clock;

	public DefaultRefreshTokenRotationStrategy(TokenService tokenService, Duration refreshTtl) {
		this(tokenService, refreshTtl, Clock.systemUTC());
	}

	public DefaultRefreshTokenRotationStrategy(TokenService tokenService, Duration refreshTtl, Clock clock) {
		this.tokenService = Objects.requireNonNull(tokenService, "tokenService");
		this.refreshTtl = normalizeRefreshTtl(refreshTtl);
		this.clock = clock == null ? Clock.systemUTC() : clock;
	}

	@Override
	public RefreshTokenRotation rotate(Principal principal, String currentRefreshToken) {
		Objects.requireNonNull(principal, "principal");
		if (Strings.isBlank(currentRefreshToken)) {
			throw new IllegalArgumentException("currentRefreshToken must not be blank");
		}
		return new RefreshTokenRotation(
			currentRefreshToken,
			tokenService.issueRefreshToken(principal),
			Instant.now(clock).plus(refreshTtl)
		);
	}

	private static Duration normalizeRefreshTtl(Duration refreshTtl) {
		return refreshTtl == null || refreshTtl.isZero() || refreshTtl.isNegative()
			? DEFAULT_REFRESH_TTL
			: refreshTtl;
	}
}
