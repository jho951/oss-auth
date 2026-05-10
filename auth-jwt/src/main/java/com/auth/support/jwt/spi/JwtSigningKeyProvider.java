package com.auth.support.jwt.spi;

import java.security.Key;

/** 새 JWT를 서명할 때 사용할 키를 제공합니다. */
public interface JwtSigningKeyProvider {

	Key signingKey();

	default String keyId() {
		return null;
	}
}
