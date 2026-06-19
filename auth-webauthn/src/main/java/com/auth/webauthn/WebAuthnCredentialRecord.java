package com.auth.webauthn;

import com.auth.core.utils.Strings;
import java.util.List;
import java.util.Map;

/** assertion 검증 시 사용하는 저장된 passkey credential 메타데이터입니다. */
public final class WebAuthnCredentialRecord {

	private final String credentialId;
	private final String userId;
	private final String publicKeyCose;
	private final long signCount;
	private final String userHandle;
	private final List<String> transports;
	private final Map<String, Object> attributes;

	public WebAuthnCredentialRecord(
		String credentialId,
		String userId,
		String publicKeyCose,
		long signCount,
		String userHandle,
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
		this.userHandle = userHandle == null ? "" : userHandle;
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

	public String getUserHandle() {
		return userHandle;
	}

	public List<String> getTransports() {
		return transports;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}
}
