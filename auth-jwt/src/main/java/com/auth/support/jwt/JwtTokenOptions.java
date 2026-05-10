package com.auth.support.jwt;

import io.jsonwebtoken.SignatureAlgorithm;
import java.time.Duration;
import java.util.Objects;

/** JWT 발급과 검증에 사용하는 불변 옵션입니다. */
public final class JwtTokenOptions {

	private final Duration accessTtl;
	private final Duration refreshTtl;
	private final SignatureAlgorithm signatureAlgorithm;
	private final String keyId;

	public JwtTokenOptions(Duration accessTtl, Duration refreshTtl, SignatureAlgorithm signatureAlgorithm, String keyId) {
		this.accessTtl = Objects.requireNonNull(accessTtl, "accessTtl");
		this.refreshTtl = Objects.requireNonNull(refreshTtl, "refreshTtl");
		this.signatureAlgorithm = signatureAlgorithm == null ? SignatureAlgorithm.HS256 : signatureAlgorithm;
		this.keyId = keyId;
	}

	public static JwtTokenOptions hs256(long accessSeconds, long refreshSeconds) {
		return new JwtTokenOptions(Duration.ofSeconds(accessSeconds), Duration.ofSeconds(refreshSeconds), SignatureAlgorithm.HS256, null);
	}

	public Duration getAccessTtl() {
		return accessTtl;
	}

	public Duration getRefreshTtl() {
		return refreshTtl;
	}

	public SignatureAlgorithm getSignatureAlgorithm() {
		return signatureAlgorithm;
	}

	public String getKeyId() {
		return keyId;
	}
}
