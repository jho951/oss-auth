package com.auth.api.model.subject;

import com.auth.api.model.AuthoritySet;
import com.auth.api.model.Principal;
import com.auth.api.model.PrincipalType;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/** Generic authenticated subject model that can represent users, services, and delegation. */
public final class AuthenticatedSubject implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String id;
	private final PrincipalType type;
	private final AuthoritySet authorities;
	private final Map<String, Object> attributes;

	public AuthenticatedSubject(String id, PrincipalType type, AuthoritySet authorities, Map<String, Object> attributes) {
		this.id = Objects.requireNonNull(id, "id");
		this.type = type == null ? PrincipalType.UNKNOWN : type;
		this.authorities = authorities == null ? AuthoritySet.empty() : authorities;
		this.attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
	}

	public static AuthenticatedSubject fromPrincipal(Principal principal, PrincipalType type) {
		Objects.requireNonNull(principal, "principal");
		return new AuthenticatedSubject(
			principal.getUserId(),
			type,
			AuthoritySet.of(principal.getAuthorities()),
			principal.getAttributes()
		);
	}

	public Principal toPrincipal() {
		return new Principal(id, authorities.asList(), attributes);
	}

	public String getId() {
		return id;
	}

	public PrincipalType getType() {
		return type;
	}

	public AuthoritySet getAuthorities() {
		return authorities;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}
}
