package com.auth.session.strategy;

import com.auth.core.api.model.Principal;
import com.auth.session.store.SessionRecord;
import java.util.Collection;
import java.util.List;

/** 동시 접속 세션 정책 */
public interface ConcurrentSessionPolicy {

	List<String> sessionsToRevoke(Principal principal, Collection<SessionRecord> activeSessions);
}
