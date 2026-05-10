package com.auth.webauthn;

import java.util.Map;

/** principal 매핑에 사용할 수 있도록 정규화된 WebAuthn assertion 검증 결과입니다. */
public final class WebAuthnAssertionResult {

	private final String credentialId;
	private final String userId;
	private final long newSignCount;
	private final Map<String, Object> attributes;

	public WebAuthnAssertionResult(String credentialId, String userId, long newSignCount, Map<String, Object> attributes) {
		if (credentialId == null || credentialId.isBlank()) throw new IllegalArgumentException("credentialId must not be blank");
		if (userId == null || userId.isBlank()) throw new IllegalArgumentException("userId must not be blank");
		this.credentialId = credentialId;
		this.userId = userId;
		this.newSignCount = newSignCount;
		this.attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
	}

	public String getCredentialId() {
		return credentialId;
	}

	public String getUserId() {
		return userId;
	}

	public long getNewSignCount() {
		return newSignCount;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}
}
