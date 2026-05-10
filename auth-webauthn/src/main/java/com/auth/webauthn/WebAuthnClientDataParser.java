package com.auth.webauthn;

/** `clientDataJSON` 문자열을 정규화된 WebAuthn 클라이언트 데이터로 파싱합니다. */
public interface WebAuthnClientDataParser {

	WebAuthnClientData parse(String clientDataJson);
}
