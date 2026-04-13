package com.auth.session;

import java.util.Optional;

/** Extended session store contract for implementations that preserve session metadata. */
public interface SessionRecordStore extends SessionStore {

	void save(SessionRecord session);

	Optional<SessionRecord> findRecord(String sessionId);
}
