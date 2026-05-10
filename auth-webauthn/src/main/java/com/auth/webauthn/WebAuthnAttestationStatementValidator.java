package com.auth.webauthn;

import java.util.Optional;

/** attestation statement의 실제 형식과 신뢰 체인 검증을 담당합니다. */
public interface WebAuthnAttestationStatementValidator {

	Optional<WebAuthnAttestationResult> verify(WebAuthnRegistrationRequest request, WebAuthnClientData clientData);
}
