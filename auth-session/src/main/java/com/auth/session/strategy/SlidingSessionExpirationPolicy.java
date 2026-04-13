package com.auth.session.strategy;

import com.auth.session.SessionRecord;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/** Sliding timeout from the latest access. */
public final class SlidingSessionExpirationPolicy implements SessionExpirationPolicy {

	private final Duration ttl;

	public SlidingSessionExpirationPolicy(Duration ttl) {
		this.ttl = Objects.requireNonNull(ttl, "ttl");
	}

	@Override
	public Instant expiresAt(SessionRecord current, Instant now) {
		return (now == null ? Instant.now() : now).plus(ttl);
	}
}
