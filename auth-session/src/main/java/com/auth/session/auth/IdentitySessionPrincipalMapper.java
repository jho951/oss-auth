package com.auth.session.auth;

import com.auth.core.api.model.Principal;
import java.util.Map;

/** Identity mapper that simply returns the stored principal. */
public final class IdentitySessionPrincipalMapper implements SessionPrincipalMapper {

    @Override
    public Principal map(String sessionId, Principal existing, Map<String, Object> attributes) {
        return existing;
    }
}
