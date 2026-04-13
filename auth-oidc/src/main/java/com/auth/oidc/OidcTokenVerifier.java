package com.auth.oidc;

/** Verifies an OIDC ID token and returns normalized identity claims. */
public interface OidcTokenVerifier {

	OidcIdentity verify(OidcAuthenticationRequest request);
}
