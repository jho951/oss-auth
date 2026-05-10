package com.auth.webauthn;

/** 인코딩된 authenticator data를 정규화된 구조로 파싱합니다. */
public interface WebAuthnAuthenticatorDataParser {

	WebAuthnAuthenticatorData parse(String authenticatorData);
}
