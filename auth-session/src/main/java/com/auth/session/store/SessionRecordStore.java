package com.auth.session.store;

import java.util.Optional;

/** 세션의 상태와 메타데이터 저장소 */
public interface SessionRecordStore extends SessionStore {

	void save(SessionRecord session);

	Optional<SessionRecord> findRecord(String sessionId);
}
