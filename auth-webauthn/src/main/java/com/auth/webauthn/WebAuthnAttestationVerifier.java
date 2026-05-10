package com.auth.webauthn;

import java.util.Optional;

/** WebAuthn attestation을 검증하고 저장 가능한 passkey credential을 추출합니다. */
public interface WebAuthnAttestationVerifier {

	Optional<WebAuthnAttestationResult> verify(WebAuthnRegistrationRequest request);
}
