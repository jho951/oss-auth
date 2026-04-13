package com.auth.oidc;

import java.util.Map;
import java.util.Objects;

/** Verified OIDC identity claims. */
public record OidcIdentity(String subject, String issuer, String audience, Map<String, Object> claims) {

	public OidcIdentity {
		Objects.requireNonNull(subject, "subject");
		claims = claims == null ? Map.of() : Map.copyOf(claims);
	}
}
