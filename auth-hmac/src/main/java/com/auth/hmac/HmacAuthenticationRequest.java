package com.auth.hmac;

import java.time.Instant;
import java.util.Map;

/** Canonical HMAC authentication input independent of HTTP framework details. */
public record HmacAuthenticationRequest(
	String keyId,
	String method,
	String path,
	byte[] body,
	Map<String, String> signedHeaders,
	String signature,
	Instant timestamp
) {
	public HmacAuthenticationRequest {
		body = body == null ? new byte[0] : body.clone();
		signedHeaders = signedHeaders == null ? Map.of() : Map.copyOf(signedHeaders);
	}

	@Override
	public byte[] body() {
		return body.clone();
	}
}
