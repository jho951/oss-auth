package com.auth.oidc;

/** OIDC token input independent of provider-specific policy. */
public final class OidcAuthenticationRequest {

	private final String idToken;
	private final String nonce;

	public OidcAuthenticationRequest(String idToken, String nonce) {
		this.idToken = idToken;
		this.nonce = nonce;
	}

	public String idToken() {
		return idToken;
	}

	public String nonce() {
		return nonce;
	}
}
