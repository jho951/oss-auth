package com.auth.webauthn;

import com.auth.core.utils.Strings;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** 공통 WebAuthn clientData/authenticatorData 검사를 수행하는 기본 assertion verifier입니다. */
public final class DefaultWebAuthnAssertionVerifier implements WebAuthnAssertionVerifier {

	private final WebAuthnClientDataParser clientDataParser;
	private final WebAuthnAuthenticatorDataParser authenticatorDataParser;
	private final WebAuthnAssertionSignatureValidator signatureValidator;

	public DefaultWebAuthnAssertionVerifier(WebAuthnAssertionSignatureValidator signatureValidator) {
		this(new JsonWebAuthnClientDataParser(), new DefaultWebAuthnAuthenticatorDataParser(), signatureValidator);
	}

	public DefaultWebAuthnAssertionVerifier(
		WebAuthnClientDataParser clientDataParser,
		WebAuthnAuthenticatorDataParser authenticatorDataParser,
		WebAuthnAssertionSignatureValidator signatureValidator
	) {
		this.clientDataParser = Objects.requireNonNull(clientDataParser, "clientDataParser");
		this.authenticatorDataParser = Objects.requireNonNull(authenticatorDataParser, "authenticatorDataParser");
		this.signatureValidator = Objects.requireNonNull(signatureValidator, "signatureValidator");
	}

	@Override
	public Optional<WebAuthnAssertionResult> verify(WebAuthnAuthenticationRequest request, WebAuthnCredentialRecord credentialRecord) {
		if (request == null || credentialRecord == null) return Optional.empty();
		try {
			if (!credentialRecord.getCredentialId().equals(request.getCredentialId())) return Optional.empty();
			if (!userHandleMatches(request, credentialRecord)) return Optional.empty();

			WebAuthnClientData clientData = clientDataParser.parse(request.getClientDataJson());
			if (!"webauthn.get".equals(clientData.type())) return Optional.empty();
			if (!request.getChallenge().equals(clientData.challenge())) return Optional.empty();
			if (!request.getOrigin().equals(clientData.origin())) return Optional.empty();

			WebAuthnAuthenticatorData authenticatorData = authenticatorDataParser.parse(request.getAuthenticatorData());
			if (!authenticatorData.isUserPresent()) return Optional.empty();
			if (requiresUserVerification(request) && !authenticatorData.isUserVerified()) return Optional.empty();
			if (!matchesRpId(request.getRpId(), authenticatorData.getRpIdHash())) return Optional.empty();
			if (credentialRecord.getSignCount() > 0 && authenticatorData.getSignCount() <= credentialRecord.getSignCount()) {
				return Optional.empty();
			}

			byte[] clientDataHash = sha256(request.getClientDataJson().getBytes(StandardCharsets.UTF_8));
			if (!signatureValidator.verify(request, credentialRecord, clientData, authenticatorData, clientDataHash)) {
				return Optional.empty();
			}

			return Optional.of(new WebAuthnAssertionResult(
				credentialRecord.getCredentialId(),
				credentialRecord.getUserId(),
				authenticatorData.getSignCount(),
				com.auth.core.utils.CollectionUtils.mapOf(
					"user_present", authenticatorData.isUserPresent(),
					"user_verified", authenticatorData.isUserVerified(),
					"origin", clientData.origin(),
					"challenge", clientData.challenge()
				)
			));
		} catch (RuntimeException e) {
			return Optional.empty();
		}
	}

	private static boolean userHandleMatches(WebAuthnAuthenticationRequest request, WebAuthnCredentialRecord credentialRecord) {
		if (Strings.isBlank(request.getUserHandle()) || Strings.isBlank(credentialRecord.getUserHandle())) return true;
		return request.getUserHandle().equals(credentialRecord.getUserHandle());
	}

	private static boolean requiresUserVerification(WebAuthnAuthenticationRequest request) {
		return Boolean.TRUE.equals(request.getAttributes().get("require_user_verification"));
	}

	private static boolean matchesRpId(String rpId, byte[] rpIdHash) {
		return Arrays.equals(sha256(rpId.getBytes(StandardCharsets.UTF_8)), rpIdHash);
	}

	private static byte[] sha256(byte[] value) {
		try {
			return MessageDigest.getInstance("SHA-256").digest(value);
		} catch (Exception e) {
			throw new IllegalStateException("failed to compute SHA-256", e);
		}
	}
}
