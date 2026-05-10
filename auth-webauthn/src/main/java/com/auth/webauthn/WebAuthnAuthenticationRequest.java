package com.auth.webauthn;

import java.util.Map;

/** WebAuthn 로그인 절차에서 추출한 정규화된 assertion 요청 데이터입니다. */
public final class WebAuthnAuthenticationRequest {

	private final String credentialId;
	private final String clientDataJson;
	private final String authenticatorData;
	private final String signature;
	private final String userHandle;
	private final String challenge;
	private final String origin;
	private final String rpId;
	private final Map<String, Object> attributes;

	public WebAuthnAuthenticationRequest(
		String credentialId,
		String clientDataJson,
		String authenticatorData,
		String signature,
		String userHandle,
		String challenge,
		String origin,
		String rpId,
		Map<String, Object> attributes
	) {
		this.credentialId = credentialId == null ? "" : credentialId.trim();
		this.clientDataJson = clientDataJson == null ? "" : clientDataJson;
		this.authenticatorData = authenticatorData == null ? "" : authenticatorData;
		this.signature = signature == null ? "" : signature;
		this.userHandle = userHandle == null ? "" : userHandle;
		this.challenge = challenge == null ? "" : challenge;
		this.origin = origin == null ? "" : origin;
		this.rpId = rpId == null ? "" : rpId;
		this.attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
	}

	public String getCredentialId() {
		return credentialId;
	}

	public String getClientDataJson() {
		return clientDataJson;
	}

	public String getAuthenticatorData() {
		return authenticatorData;
	}

	public String getSignature() {
		return signature;
	}

	public String getUserHandle() {
		return userHandle;
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
