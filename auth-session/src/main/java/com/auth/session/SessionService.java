package com.auth.session;

import com.auth.core.api.model.Principal;
import com.auth.core.utils.Strings;
import com.auth.session.id.SessionIdGenerator;
import com.auth.session.store.SessionStore;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

/** Helper service that issues and revokes session identifiers. */
public final class SessionService {

    private final SessionStore sessionStore;
    private final SessionIdGenerator idGenerator;
    private final Duration sessionTtl;

    public SessionService(SessionStore sessionStore, SessionIdGenerator idGenerator, Duration sessionTtl) {
        this.sessionStore = Objects.requireNonNull(sessionStore, "sessionStore");
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
        this.sessionTtl = sessionTtl != null ? sessionTtl : Duration.ofHours(1);
    }

    public String create(Principal principal) {
        Objects.requireNonNull(principal, "principal");
        String sessionId = idGenerator.generate();
        sessionStore.save(sessionId, principal);
        return sessionId;
    }

    /** Resolve the principal associated with the session. */
    public Optional<Principal> resolve(String sessionId) {
		if (Strings.isBlank(sessionId)) return Optional.empty();
        return sessionStore.find(sessionId);
    }

    /** Revoke the session identifier. */
    public void revoke(String sessionId) {
		if (Strings.isBlank(sessionId)) return;
        sessionStore.revoke(sessionId);
    }

    public Duration getSessionTtl() {
        return sessionTtl;
    }
}
