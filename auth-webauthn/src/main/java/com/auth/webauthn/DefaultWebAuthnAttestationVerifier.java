package com.auth.webauthn;

import java.util.Objects;
import java.util.Optional;

/** 공통 clientData 검증 뒤 attestation statement 검증을 위임하는 기본 verifier입니다. */
public final class DefaultWebAuthnAttestationVerifier implements WebAuthnAttestationVerifier {

	private final WebAuthnClientDataParser clientDataParser;
	private final WebAuthnAttestationStatementValidator attestationStatementValidator;

	public DefaultWebAuthnAttestationVerifier(WebAuthnAttestationStatementValidator attestationStatementValidator) {
		this(new JsonWebAuthnClientDataParser(), attestationStatementValidator);
	}

	public DefaultWebAuthnAttestationVerifier(
		WebAuthnClientDataParser clientDataParser,
		WebAuthnAttestationStatementValidator attestationStatementValidator
	) {
		this.clientDataParser = Objects.requireNonNull(clientDataParser, "clientDataParser");
		this.attestationStatementValidator = Objects.requireNonNull(attestationStatementValidator, "attestationStatementValidator");
	}

	@Override
	public Optional<WebAuthnAttestationResult> verify(WebAuthnRegistrationRequest request) {
		if (request == null) return Optional.empty();
		try {
			WebAuthnClientData clientData = clientDataParser.parse(request.getClientDataJson());
			if (!"webauthn.create".equals(clientData.type())) return Optional.empty();
			if (!request.getChallenge().equals(clientData.challenge())) return Optional.empty();
			if (!request.getOrigin().equals(clientData.origin())) return Optional.empty();
			return attestationStatementValidator.verify(request, clientData);
		} catch (RuntimeException e) {
			return Optional.empty();
		}
	}
}
