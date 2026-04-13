package com.auth.support.jwt.spi;

import java.security.Key;

/** Supplies the signing key used when issuing a JWT. */
public interface JwtSigningKeyProvider {

	Key signingKey();

	default String keyId() {
		return null;
	}
}
