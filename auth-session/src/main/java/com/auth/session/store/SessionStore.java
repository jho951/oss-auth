package com.auth.session.store;

import com.auth.core.api.model.Principal;
import java.util.Optional;

/** 접속 주체 저장소 */
public interface SessionStore {

    void save(String sessionId, Principal principal);

    Optional<Principal> find(String sessionId);

    void revoke(String sessionId);
}
