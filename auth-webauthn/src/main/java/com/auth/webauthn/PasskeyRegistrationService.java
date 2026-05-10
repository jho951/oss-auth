package com.auth.webauthn;

import java.util.Objects;
import java.util.Optional;

/** attestation 데이터를 검증하고 저장 가능한 credential 결과를 반환합니다. */
public final class PasskeyRegistrationService {

	private final WebAuthnAttestationVerifier attestationVerifier;
	private final DefaultWebAuthnRequestValidator requestValidator;

	public PasskeyRegistrationService(WebAuthnAttestationVerifier attestationVerifier) {
		this(attestationVerifier, new DefaultWebAuthnRequestValidator());
	}

	public PasskeyRegistrationService(
		WebAuthnAttestationVerifier attestationVerifier,
		DefaultWebAuthnRequestValidator requestValidator
	) {
		this.attestationVerifier = Objects.requireNonNull(attestationVerifier, "attestationVerifier");
		this.requestValidator = requestValidator == null ? new DefaultWebAuthnRequestValidator() : requestValidator;
	}

	public Optional<WebAuthnAttestationResult> register(WebAuthnRegistrationRequest request) {
		if (!requestValidator.isValid(request)) return Optional.empty();
		return attestationVerifier.verify(request);
	}
}
