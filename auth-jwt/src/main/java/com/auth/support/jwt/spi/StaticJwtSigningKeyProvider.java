package com.auth.support.jwt.spi;

import java.security.Key;
import java.util.Objects;

/** 서명 키를 회전하지 않는 배포를 위한 단일 키 signing provider입니다. */
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
