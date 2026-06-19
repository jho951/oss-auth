package com.auth.apikey;

import java.util.Objects;

/** API key credential value with optional key identifier. */
public final class ApiKeyCredential {

	private final String keyId;
	private final String secret;

	public ApiKeyCredential(String keyId, String secret) {
		this.keyId = keyId;
		this.secret = Objects.requireNonNull(secret, "secret");
	}

	public String keyId() {
		return keyId;
	}
	public String secret() {
		return secret;
	}
}
