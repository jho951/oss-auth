package com.auth.session.auth;

import com.auth.core.api.model.Principal;
import java.util.Map;

/** Mapper from session metadata into {@link Principal} instances. */
public interface SessionPrincipalMapper {

    /**
     * Build a principal representing the user behind the session.
     * @param existing the principal retrieved from the store
     */
    Principal map(String sessionId, Principal existing, Map<String, Object> attributes);
}
