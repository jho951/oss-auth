package com.auth.api.model.subject;

import com.auth.api.model.AuthoritySet;
import com.auth.api.model.PrincipalType;
import java.util.Map;

/** Factory for anonymous subjects. */
public final class AnonymousPrincipal {

	private AnonymousPrincipal() {
	}

	public static AuthenticatedSubject create() {
		return new AuthenticatedSubject("anonymous", PrincipalType.ANONYMOUS, AuthoritySet.empty(), Map.of());
	}
}
