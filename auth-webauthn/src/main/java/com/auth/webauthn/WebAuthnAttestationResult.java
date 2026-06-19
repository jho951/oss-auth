package com.auth.webauthn;

import com.auth.core.utils.Strings;
import java.util.List;
import java.util.Map;

/** 새 passkey credential 저장에 사용할 수 있는 attestation 검증 결과입니다. */
public final class WebAuthnAttestationResult {

	private final String credentialId;
	private final String userId;
	private final String publicKeyCose;
	private final long signCount;
	private final List<String> transports;
	private final Map<String, Object> attributes;

	public WebAuthnAttestationResult(
		String credentialId,
		String userId,
		String publicKeyCose,
		long signCount,
		List<String> transports,
		Map<String, Object> attributes
	) {
		if (Strings.isBlank(credentialId)) throw new IllegalArgumentException("credentialId must not be blank");
		if (Strings.isBlank(userId)) throw new IllegalArgumentException("userId must not be blank");
		if (Strings.isBlank(publicKeyCose)) throw new IllegalArgumentException("publicKeyCose must not be blank");
		this.credentialId = credentialId;
		this.userId = userId;
		this.publicKeyCose = publicKeyCose;
		this.signCount = signCount;
		this.transports = transports == null ? com.auth.core.utils.CollectionUtils.listOf() : com.auth.core.utils.CollectionUtils.copyList(transports);
		this.attributes = attributes == null ? com.auth.core.utils.CollectionUtils.mapOf() : com.auth.core.utils.CollectionUtils.copyMap(attributes);
	}

	public String getCredentialId() {
		return credentialId;
	}

	public String getUserId() {
		return userId;
	}

	public String getPublicKeyCose() {
		return publicKeyCose;
	}

	public long getSignCount() {
		return signCount;
	}

	public List<String> getTransports() {
		return transports;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}
}
