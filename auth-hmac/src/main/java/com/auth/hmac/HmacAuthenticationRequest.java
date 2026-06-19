package com.auth.hmac;

import java.time.Instant;
import java.util.Map;

/** Canonical HMAC authentication input independent of HTTP framework details. */
public final class HmacAuthenticationRequest {

	private final String keyId;
	private final String method;
	private final String path;
	private final byte[] body;
	private final Map<String, String> signedHeaders;
	private final String signature;
	private final Instant timestamp;

	public HmacAuthenticationRequest(
		String keyId,
		String method,
		String path,
		byte[] body,
		Map<String, String> signedHeaders,
		String signature,
		Instant timestamp
	) {
		this.keyId = keyId;
		this.method = method;
		this.path = path;
		this.body = body == null ? new byte[0] : body.clone();
		this.signedHeaders = signedHeaders == null ? com.auth.core.utils.CollectionUtils.mapOf() : com.auth.core.utils.CollectionUtils.copyMap(signedHeaders);
		this.signature = signature;
		this.timestamp = timestamp;
	}

	public String keyId() {
		return keyId;
	}

	public String method() {
		return method;
	}

	public String path() {
		return path;
	}

	public byte[] body() {
		return body.clone();
	}

	public Map<String, String> signedHeaders() {
		return signedHeaders;
	}

	public String signature() {
		return signature;
	}

	public Instant timestamp() {
		return timestamp;
	}
}
