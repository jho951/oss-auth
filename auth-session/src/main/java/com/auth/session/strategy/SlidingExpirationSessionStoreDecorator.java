package com.auth.session.strategy;

import com.auth.api.model.Principal;
import com.auth.session.SessionRecord;
import com.auth.session.SessionRecordStore;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/** Decorator that refreshes expiration metadata when a session is read. */
public final class SlidingExpirationSessionStoreDecorator implements SessionRecordStore {

	private final SessionRecordStore delegate;
	private final SessionExpirationPolicy expirationPolicy;
	private final Clock clock;

	public SlidingExpirationSessionStoreDecorator(SessionRecordStore delegate, SessionExpirationPolicy expirationPolicy) {
		this(delegate, expirationPolicy, Clock.systemUTC());
	}

	public SlidingExpirationSessionStoreDecorator(SessionRecordStore delegate, SessionExpirationPolicy expirationPolicy, Clock clock) {
		this.delegate = Objects.requireNonNull(delegate, "delegate");
		this.expirationPolicy = Objects.requireNonNull(expirationPolicy, "expirationPolicy");
		this.clock = clock == null ? Clock.systemUTC() : clock;
	}

	@Override
	public void save(String sessionId, Principal principal) {
		save(SessionRecord.active(sessionId, principal));
	}

	@Override
	public void save(SessionRecord session) {
		Instant now = clock.instant();
		delegate.save(session.accessedAt(now, expirationPolicy.expiresAt(session, now)));
	}

	@Override
	public Optional<Principal> find(String sessionId) {
		return findRecord(sessionId).map(SessionRecord::getPrincipal);
	}

	@Override
	public Optional<SessionRecord> findRecord(String sessionId) {
		Instant now = clock.instant();
		Optional<SessionRecord> found = delegate.findRecord(sessionId);
		if (found.isEmpty()) return Optional.empty();
		SessionRecord record = found.get();
		if (expirationPolicy.isExpired(record, now)) {
			delegate.revoke(sessionId);
			return Optional.empty();
		}
		SessionRecord refreshed = record.accessedAt(now, expirationPolicy.expiresAt(record, now));
		delegate.save(refreshed);
		return Optional.of(refreshed);
	}

	@Override
	public void revoke(String sessionId) {
		delegate.revoke(sessionId);
	}
}
