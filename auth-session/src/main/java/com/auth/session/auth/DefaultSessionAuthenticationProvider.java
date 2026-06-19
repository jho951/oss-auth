package com.auth.session.auth;

import com.auth.core.api.model.Principal;
import com.auth.core.utils.Strings;
import com.auth.session.store.SessionStore;
import java.util.Optional;
import java.util.Collections;
import java.util.Objects;

/** Default {@link SessionAuthenticationProvider} that uses a {@link SessionStore} and mapper. */
public final class DefaultSessionAuthenticationProvider implements SessionAuthenticationProvider {

    private final SessionStore sessionStore;
    private final SessionPrincipalMapper principalMapper;

    public DefaultSessionAuthenticationProvider(SessionStore sessionStore, SessionPrincipalMapper principalMapper) {
        this.sessionStore = Objects.requireNonNull(sessionStore, "sessionStore");
        this.principalMapper = Objects.requireNonNull(principalMapper, "principalMapper");
    }

    @Override
    public Optional<Principal> authenticate(String sessionId) {
		if (Strings.isBlank(sessionId)) return Optional.empty();
        return sessionStore.find(sessionId).map(existing -> principalMapper.map(sessionId, existing, Collections.emptyMap()));
    }
}
