package com.auth.oidc;

/** OIDC token input independent of provider-specific policy. */
public record OidcAuthenticationRequest(String idToken, String nonce) {
}
