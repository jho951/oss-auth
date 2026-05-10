package com.auth.webauthn;

import java.util.Optional;

/** 저장된 credential을 기준으로 WebAuthn assertion을 검증합니다. */
public interface WebAuthnAssertionVerifier {

	Optional<WebAuthnAssertionResult> verify(WebAuthnAuthenticationRequest request, WebAuthnCredentialRecord credentialRecord);
}
