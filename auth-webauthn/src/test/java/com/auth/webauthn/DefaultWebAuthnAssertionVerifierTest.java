package com.auth.webauthn;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DefaultWebAuthnAssertionVerifierTest {

	@Test
	void verifiesMatchingAssertion() {
		DefaultWebAuthnAssertionVerifier verifier = new DefaultWebAuthnAssertionVerifier(
			(request, credentialRecord, clientData, authenticatorData, clientDataHash) ->
				clientDataHash.length == 32
					&& authenticatorData.getSignCount() == 8
					&& "public-key".equals(credentialRecord.getPublicKeyCose())
		);

		Optional<WebAuthnAssertionResult> result = verifier.verify(
			new WebAuthnAuthenticationRequest(
				"cred-1",
				"{\"type\":\"webauthn.get\",\"challenge\":\"challenge-1\",\"origin\":\"https://example.com\"}",
				authenticatorData("example.com", 0x05, 8),
				"signature",
				"handle-1",
				"challenge-1",
				"https://example.com",
				"example.com",
				com.auth.core.utils.CollectionUtils.mapOf("require_user_verification", true)
			),
			new WebAuthnCredentialRecord("cred-1", "user-1", "public-key", 7, "handle-1", com.auth.core.utils.CollectionUtils.listOf("internal"), com.auth.core.utils.CollectionUtils.mapOf())
		);

		assertThat(result).isPresent();
		assertThat(result.get().getNewSignCount()).isEqualTo(8);
		assertThat(result.get().getAttributes().get("user_verified")).isEqualTo(true);
	}

	@Test
	void rejectsRollbackedSignCount() {
		DefaultWebAuthnAssertionVerifier verifier = new DefaultWebAuthnAssertionVerifier(
			(request, credentialRecord, clientData, authenticatorData, clientDataHash) -> true
		);

		Optional<WebAuthnAssertionResult> result = verifier.verify(
			new WebAuthnAuthenticationRequest(
				"cred-1",
				"{\"type\":\"webauthn.get\",\"challenge\":\"challenge-1\",\"origin\":\"https://example.com\"}",
				authenticatorData("example.com", 0x01, 7),
				"signature",
				"",
				"challenge-1",
				"https://example.com",
				"example.com",
				com.auth.core.utils.CollectionUtils.mapOf()
			),
			new WebAuthnCredentialRecord("cred-1", "user-1", "public-key", 7, "", com.auth.core.utils.CollectionUtils.listOf("internal"), com.auth.core.utils.CollectionUtils.mapOf())
		);

		assertThat(result).isEmpty();
	}

	private static String authenticatorData(String rpId, int flags, long signCount) {
		try {
			byte[] rpIdHash = MessageDigest.getInstance("SHA-256").digest(rpId.getBytes(StandardCharsets.UTF_8));
			ByteBuffer buffer = ByteBuffer.allocate(37);
			buffer.put(rpIdHash);
			buffer.put((byte) flags);
			buffer.putInt((int) signCount);
			return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer.array());
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
