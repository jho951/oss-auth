package com.auth.session.store;

import com.auth.core.api.model.Principal;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Test-only in-memory store used to verify session contracts without external infrastructure. */
public final class TestSessionStore implements SessionRecordStore {

    private final Map<String, SessionRecord> sessions = new ConcurrentHashMap<>();

    @Override
    public void save(String sessionId, Principal principal) {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(principal, "principal");
        save(SessionRecord.active(sessionId, principal));
    }

    @Override
    public void save(SessionRecord session) {
        Objects.requireNonNull(session, "session");
        sessions.put(session.getSessionId(), session);
    }

    @Override
    public Optional<Principal> find(String sessionId) {
        return findRecord(sessionId).map(SessionRecord::getPrincipal);
    }

    @Override
    public Optional<SessionRecord> findRecord(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    @Override
    public void revoke(String sessionId) {
        sessions.remove(sessionId);
    }
}
