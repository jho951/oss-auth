package com.auth.session.strategy;

import com.auth.session.SessionRecord;
import java.time.Instant;

/** Generic expiration strategy for session engines. */
public interface SessionExpirationPolicy {

	Instant expiresAt(SessionRecord current, Instant now);

	default boolean isExpired(SessionRecord current, Instant now) {
		return current != null && current.isExpired(now);
	}
}
