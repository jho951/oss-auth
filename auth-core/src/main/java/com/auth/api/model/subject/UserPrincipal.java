package com.auth.api.model.subject;

import com.auth.api.model.AuthoritySet;
import com.auth.api.model.PrincipalType;
import java.util.Map;

/** Factory for user subjects. */
public final class UserPrincipal {

	private UserPrincipal() {
	}

	public static AuthenticatedSubject of(String userId, AuthoritySet authorities, Map<String, Object> attributes) {
		return new AuthenticatedSubject(userId, PrincipalType.USER, authorities, attributes);
	}
}
