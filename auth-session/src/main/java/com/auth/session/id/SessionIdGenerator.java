package com.auth.session.id;

import com.auth.session.store.SessionStore;

/** 세션 ID 발급기 */
public interface SessionIdGenerator {

    String generate();
}
