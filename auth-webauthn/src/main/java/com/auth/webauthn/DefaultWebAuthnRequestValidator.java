package com.auth.webauthn;

import com.auth.core.utils.Strings;

/** 암호학 검증 전에 등록/인증 흐름이 공통으로 수행하는 최소 검증기입니다. */
public final class DefaultWebAuthnRequestValidator {

	public boolean isValid(WebAuthnAuthenticationRequest request) {
		return request != null
			&& !Strings.isBlank(request.getCredentialId())
			&& !Strings.isBlank(request.getClientDataJson())
			&& !Strings.isBlank(request.getAuthenticatorData())
			&& !Strings.isBlank(request.getSignature())
			&& !Strings.isBlank(request.getChallenge())
			&& !Strings.isBlank(request.getOrigin())
			&& !Strings.isBlank(request.getRpId());
	}

	public boolean isValid(WebAuthnRegistrationRequest request) {
		return request != null
			&& !Strings.isBlank(request.getCredentialId())
			&& !Strings.isBlank(request.getClientDataJson())
			&& !Strings.isBlank(request.getAttestationObject())
			&& !Strings.isBlank(request.getChallenge())
			&& !Strings.isBlank(request.getOrigin())
			&& !Strings.isBlank(request.getRpId());
	}
}
