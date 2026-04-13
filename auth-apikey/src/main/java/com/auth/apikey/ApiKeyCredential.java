package com.auth.apikey;

import java.util.Objects;

/** API key credential value with optional key identifier. */
public record ApiKeyCredential(String keyId, String secret) {

	public ApiKeyCredential {
		Objects.requireNonNull(secret, "secret");
	}
}
