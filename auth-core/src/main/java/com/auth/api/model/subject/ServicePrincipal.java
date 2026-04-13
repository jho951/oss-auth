package com.auth.api.model.subject;

import com.auth.api.model.AuthoritySet;
import com.auth.api.model.PrincipalType;
import java.util.Map;

/** Factory for machine/service subjects. */
public final class ServicePrincipal {

	private ServicePrincipal() {
	}

	public static AuthenticatedSubject of(String serviceId, AuthoritySet authorities, Map<String, Object> attributes) {
		return new AuthenticatedSubject(serviceId, PrincipalType.SERVICE, authorities, attributes);
	}
}
