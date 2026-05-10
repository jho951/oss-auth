package com.auth.session.auth;

import com.auth.core.api.model.Principal;
import java.util.Optional;

/** Strategy for authenticating a request backed by a session identifier */
public interface SessionAuthenticationProvider {

    /** Authenticate by retrieving the principal associated with the sessionId. */
    Optional<Principal> authenticate(String sessionId);
}
