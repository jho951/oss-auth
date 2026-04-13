package com.auth.session.strategy;

import com.auth.session.SessionRecord;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/** Absolute timeout from session creation. */
public final class FixedSessionExpirationPolicy implements SessionExpirationPolicy {

	private final Duration ttl;

	public FixedSessionExpirationPolicy(Duration ttl) {
		this.ttl = Objects.requireNonNull(ttl, "ttl");
	}

	@Override
	public Instant expiresAt(SessionRecord current, Instant now) {
		Instant base = current == null ? now : current.getCreatedAt();
		return base.plus(ttl);
	}
}
