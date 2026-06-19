package com.auth.webauthn;

import java.util.Map;

/** passkey 등록 절차에서 추출한 정규화된 attestation 요청 데이터입니다. */
public final class WebAuthnRegistrationRequest {

	private final String credentialId;
	private final String clientDataJson;
	private final String attestationObject;
	private final String challenge;
	private final String origin;
	private final String rpId;
	private final Map<String, Object> attributes;

	public WebAuthnRegistrationRequest(
		String credentialId,
		String clientDataJson,
		String attestationObject,
		String challenge,
		String origin,
		String rpId,
		Map<String, Object> attributes
	) {
		this.credentialId = credentialId == null ? "" : credentialId.trim();
		this.clientDataJson = clientDataJson == null ? "" : clientDataJson;
		this.attestationObject = attestationObject == null ? "" : attestationObject;
		this.challenge = challenge == null ? "" : challenge;
		this.origin = origin == null ? "" : origin;
		this.rpId = rpId == null ? "" : rpId;
		this.attributes = attributes == null ? com.auth.core.utils.CollectionUtils.mapOf() : com.auth.core.utils.CollectionUtils.copyMap(attributes);
	}

	public String getCredentialId() {
		return credentialId;
	}

	public String getClientDataJson() {
		return clientDataJson;
	}

	public String getAttestationObject() {
		return attestationObject;
	}

	public String getChallenge() {
		return challenge;
	}

	public String getOrigin() {
		return origin;
	}

	public String getRpId() {
		return rpId;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}
}
