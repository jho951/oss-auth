package com.auth.session;

import com.auth.api.model.Principal;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** A basic in-memory {@link SessionStore} meant for early-session support. */
public final class SimpleSessionStore implements SessionRecordStore {

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

    public Collection<SessionRecord> records() {
        return sessions.values();
    }
}
