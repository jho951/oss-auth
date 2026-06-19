package com.auth.oidc;

import java.util.Map;
import java.util.Objects;

/** Verified OIDC identity claims. */
public final class OidcIdentity {

	private final String subject;
	private final String issuer;
	private final String audience;
	private final Map<String, Object> claims;

	public OidcIdentity(String subject, String issuer, String audience, Map<String, Object> claims) {
		this.subject = Objects.requireNonNull(subject, "subject");
		this.issuer = issuer;
		this.audience = audience;
		this.claims = claims == null ? com.auth.core.utils.CollectionUtils.mapOf() : com.auth.core.utils.CollectionUtils.copyMap(claims);
	}

	public String subject() {
		return subject;
	}

	public String issuer() {
		return issuer;
	}

	public String audience() {
		return audience;
	}

	public Map<String, Object> claims() {
		return claims;
	}
}
