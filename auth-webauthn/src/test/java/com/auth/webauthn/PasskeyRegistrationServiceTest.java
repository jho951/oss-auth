package com.auth.webauthn;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PasskeyRegistrationServiceTest {

	@Test
	void registersValidAttestation() {
		PasskeyRegistrationService service = new PasskeyRegistrationService(request -> Optional.of(
			new WebAuthnAttestationResult("cred-1", "user-1", "public-key", 0, List.of("hybrid"), Map.of("aaguid", "aaguid-1"))
		));

		Optional<WebAuthnAttestationResult> result = service.register(new WebAuthnRegistrationRequest(
			"cred-1",
			"client-data",
			"attestation",
			"challenge",
			"https://example.com",
			"example.com",
			Map.of()
		));

		assertThat(result).isPresent();
		assertThat(result.orElseThrow().getCredentialId()).isEqualTo("cred-1");
	}
}
