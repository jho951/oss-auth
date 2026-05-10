package com.auth.webauthn;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DefaultWebAuthnAttestationVerifierTest {

	@Test
	void verifiesClientDataBeforeDelegatingAttestationValidation() {
		DefaultWebAuthnAttestationVerifier verifier = new DefaultWebAuthnAttestationVerifier(
			(request, clientData) -> Optional.of(new WebAuthnAttestationResult(
				request.getCredentialId(),
				"user-1",
				"public-key",
				0,
				List.of("hybrid"),
				Map.of("origin", clientData.origin())
			))
		);

		Optional<WebAuthnAttestationResult> result = verifier.verify(new WebAuthnRegistrationRequest(
			"cred-1",
			"""
				{"type":"webauthn.create","challenge":"challenge-1","origin":"https://example.com"}
				""",
			"attestation-object",
			"challenge-1",
			"https://example.com",
			"example.com",
			Map.of()
		));

		assertThat(result).isPresent();
		assertThat(result.orElseThrow().getAttributes().get("origin")).isEqualTo("https://example.com");
	}

	@Test
	void rejectsUnexpectedClientDataType() {
		DefaultWebAuthnAttestationVerifier verifier = new DefaultWebAuthnAttestationVerifier(
			(request, clientData) -> Optional.of(new WebAuthnAttestationResult(
				request.getCredentialId(),
				"user-1",
				"public-key",
				0,
				List.of(),
				Map.of()
			))
		);

		Optional<WebAuthnAttestationResult> result = verifier.verify(new WebAuthnRegistrationRequest(
			"cred-1",
			"""
				{"type":"webauthn.get","challenge":"challenge-1","origin":"https://example.com"}
				""",
			"attestation-object",
			"challenge-1",
			"https://example.com",
			"example.com",
			Map.of()
		));

		assertThat(result).isEmpty();
	}
}
