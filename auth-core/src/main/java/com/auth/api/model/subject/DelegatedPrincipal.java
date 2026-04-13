package com.auth.api.model.subject;

import com.auth.api.model.AuthoritySet;
import com.auth.api.model.PrincipalType;
import java.util.HashMap;
import java.util.Map;

/** Factory for delegated subjects, preserving the actor that delegated access. */
public final class DelegatedPrincipal {

	private DelegatedPrincipal() {
	}

	public static AuthenticatedSubject of(String subjectId, String actorId, AuthoritySet authorities, Map<String, Object> attributes) {
		Map<String, Object> values = new HashMap<>(attributes == null ? Map.of() : attributes);
		values.put("actor_id", actorId);
		return new AuthenticatedSubject(subjectId, PrincipalType.DELEGATED, authorities, values);
	}
}
