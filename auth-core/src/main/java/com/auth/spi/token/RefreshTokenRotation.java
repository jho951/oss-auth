package com.auth.spi.token;

import java.time.Instant;
import java.util.Objects;

/** Value object returned by a refresh token rotation strategy. */
public final class RefreshTokenRotation {

	private final String previousToken;
	private final String nextToken;
	private final Instant nextExpiresAt;

	public RefreshTokenRotation(String previousToken, String nextToken, Instant nextExpiresAt) {
		this.previousToken = previousToken;
		this.nextToken = Objects.requireNonNull(nextToken, "nextToken");
		this.nextExpiresAt = Objects.requireNonNull(nextExpiresAt, "nextExpiresAt");
	}

	public String getPreviousToken() {
		return previousToken;
	}

	public String getNextToken() {
		return nextToken;
	}

	public Instant getNextExpiresAt() {
		return nextExpiresAt;
	}
}
