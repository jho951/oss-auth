package com.auth.webauthn;

import com.auth.core.utils.Strings;

/** `clientDataJSON`에서 추출한 핵심 WebAuthn 클라이언트 데이터입니다. */
public record WebAuthnClientData(String type, String challenge, String origin, boolean crossOrigin) {

	public WebAuthnClientData {
		type = Strings.requireNonBlank(type, "type").trim();
		challenge = Strings.requireNonBlank(challenge, "challenge").trim();
		origin = Strings.requireNonBlank(origin, "origin").trim();
	}
}
