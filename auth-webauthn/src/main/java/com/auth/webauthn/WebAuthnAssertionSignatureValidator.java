package com.auth.webauthn;

/** assertion의 암호학 서명을 실제 저장된 키와 비교해 검증합니다. */
public interface WebAuthnAssertionSignatureValidator {

	boolean verify(
		WebAuthnAuthenticationRequest request,
		WebAuthnCredentialRecord credentialRecord,
		WebAuthnClientData clientData,
		WebAuthnAuthenticatorData authenticatorData,
		byte[] clientDataHash
	);
}
