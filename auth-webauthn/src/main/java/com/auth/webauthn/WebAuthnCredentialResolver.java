package com.auth.webauthn;

import java.util.Optional;

/** credential 식별자로 저장된 passkey credential을 조회합니다. */
public interface WebAuthnCredentialResolver {

	Optional<WebAuthnCredentialRecord> resolve(String credentialId);
}
