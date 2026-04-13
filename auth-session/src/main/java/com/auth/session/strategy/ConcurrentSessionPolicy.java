package com.auth.session.strategy;

import com.auth.api.model.Principal;
import com.auth.session.SessionRecord;
import java.util.Collection;
import java.util.List;

/** Generic policy hook for limiting or pruning concurrent sessions. */
public interface ConcurrentSessionPolicy {

	List<String> sessionsToRevoke(Principal principal, Collection<SessionRecord> activeSessions);
}
