package com.auth.support.jwt.spi;

import java.security.Key;
import java.util.Objects;

/** Single-key signing provider for deployments that do not rotate signing keys. */
public final class StaticJwtSigningKeyProvider implements JwtSigningKeyProvider {

	private final Key key;
	private final String keyId;

	public StaticJwtSigningKeyProvider(Key key) {
		this(key, null);
	}

	public StaticJwtSigningKeyProvider(Key key, String keyId) {
		this.key = Objects.requireNonNull(key, "key");
		this.keyId = keyId;
	}

	@Override
	public Key signingKey() {
		return key;
	}

	@Override
	public String keyId() {
		return keyId;
	}
}
