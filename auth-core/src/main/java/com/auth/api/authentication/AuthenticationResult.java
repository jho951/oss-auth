package com.auth.api.authentication;

import com.auth.api.model.Principal;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Result metadata produced by a reusable authentication capability. */
public final class AuthenticationResult {

	private final Principal principal;
	private final AuthenticationSource source;
	private final Instant authenticatedAt;
	private final Map<String, Object> attributes;

	public AuthenticationResult(Principal principal, AuthenticationSource source) {
		this(principal, source, Instant.now(), Map.of());
	}

	public AuthenticationResult(Principal principal, AuthenticationSource source, Instant authenticatedAt, Map<String, Object> attributes) {
		this.principal = Objects.requireNonNull(principal, "principal");
		this.source = Objects.requireNonNull(source, "source");
		this.authenticatedAt = authenticatedAt == null ? Instant.now() : authenticatedAt;
		this.attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
	}

	public Principal getPrincipal() {
		return principal;
	}

	public AuthenticationSource getSource() {
		return source;
	}

	public Instant getAuthenticatedAt() {
		return authenticatedAt;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public Optional<Object> getAttribute(String key) {
		return Optional.ofNullable(attributes.get(key));
	}
}
