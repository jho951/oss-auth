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
			new WebAuthnAttestationResult("cred-1", "user-1", "public-key", 0, com.auth.core.utils.CollectionUtils.listOf("hybrid"), com.auth.core.utils.CollectionUtils.mapOf("aaguid", "aaguid-1"))
		));

		Optional<WebAuthnAttestationResult> result = service.register(new WebAuthnRegistrationRequest(
			"cred-1",
			"client-data",
			"attestation",
			"challenge",
			"https://example.com",
			"example.com",
			com.auth.core.utils.CollectionUtils.mapOf()
		));

		assertThat(result).isPresent();
		assertThat(result.get().getCredentialId()).isEqualTo("cred-1");
	}
}
