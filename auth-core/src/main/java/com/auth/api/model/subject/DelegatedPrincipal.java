package com.auth.api.model.subject;

import com.auth.api.model.AuthoritySet;
import com.auth.api.model.PrincipalType;
import java.util.HashMap;
import java.util.Map;

/** Delegation을 처리 (한 주체가 다른 주체의 권한을 대신 행사할 때) */
public final class DelegatedPrincipal {

	private DelegatedPrincipal() {}

	public static AuthenticatedSubject of(String subjectId, String actorId, AuthoritySet authorities, Map<String, Object> attributes) {
		Map<String, Object> values = new HashMap<>(attributes == null ? Map.of() : attributes);
		values.put("actor_id", actorId);
		return new AuthenticatedSubject(subjectId, PrincipalType.DELEGATED, authorities, values);
	}
}
