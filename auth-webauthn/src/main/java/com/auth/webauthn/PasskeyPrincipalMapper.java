package com.auth.webauthn;

import com.auth.core.api.model.Principal;

/** 검증된 WebAuthn assertion을 공통 principal 모델로 변환합니다. */
public interface PasskeyPrincipalMapper {

	Principal map(WebAuthnAssertionResult assertionResult, WebAuthnCredentialRecord credentialRecord);
}
