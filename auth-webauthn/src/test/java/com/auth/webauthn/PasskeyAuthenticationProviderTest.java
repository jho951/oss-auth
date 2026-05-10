package com.auth.webauthn;

import static org.assertj.core.api.Assertions.assertThat;

import com.auth.core.api.model.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PasskeyAuthenticationProviderTest {

	@Test
	void authenticatesVerifiedAssertion() {
		WebAuthnCredentialRecord record = new WebAuthnCredentialRecord(
			"cred-1",
			"user-1",
			"public-key",
			7,
			"handle-1",
			List.of("internal"),
			Map.of()
		);
		PasskeyAuthenticationProvider provider = new PasskeyAuthenticationProvider(
			credentialId -> Optional.of(record),
			(request, credentialRecord) -> Optional.of(new WebAuthnAssertionResult(
				credentialRecord.getCredentialId(),
				credentialRecord.getUserId(),
				8,
				Map.of("uv", true)
			)),
			(assertionResult, credentialRecord) -> new Principal(
				assertionResult.getUserId(),
				List.of("USER"),
				Map.of("credential_id", credentialRecord.getCredentialId(), "uv", true)
			)
		);

		Optional<Principal> result = provider.authenticate(new WebAuthnAuthenticationRequest(
			"cred-1",
			"client-data",
			"auth-data",
			"sig",
			"handle-1",
			"challenge",
			"https://example.com",
			"example.com",
			Map.of()
		));

		assertThat(result).isPresent();
		assertThat(result.orElseThrow().getUserId()).isEqualTo("user-1");
		assertThat(result.orElseThrow().getAttribute("credential_id")).isEqualTo("cred-1");
	}
}
