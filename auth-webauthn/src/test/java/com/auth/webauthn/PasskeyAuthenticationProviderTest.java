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
			com.auth.core.utils.CollectionUtils.listOf("internal"),
			com.auth.core.utils.CollectionUtils.mapOf()
		);
		PasskeyAuthenticationProvider provider = new PasskeyAuthenticationProvider(
			credentialId -> Optional.of(record),
			(request, credentialRecord) -> Optional.of(new WebAuthnAssertionResult(
				credentialRecord.getCredentialId(),
				credentialRecord.getUserId(),
				8,
				com.auth.core.utils.CollectionUtils.mapOf("uv", true)
			)),
			(assertionResult, credentialRecord) -> new Principal(
				assertionResult.getUserId(),
				com.auth.core.utils.CollectionUtils.listOf("USER"),
				com.auth.core.utils.CollectionUtils.mapOf("credential_id", credentialRecord.getCredentialId(), "uv", true)
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
			com.auth.core.utils.CollectionUtils.mapOf()
		));

		assertThat(result).isPresent();
		assertThat(result.get().getUserId()).isEqualTo("user-1");
		assertThat(result.get().getAttribute("credential_id")).isEqualTo("cred-1");
	}
}
