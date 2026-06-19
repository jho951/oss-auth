package com.auth.webauthn;

import com.auth.core.utils.Strings;

/** `clientDataJSON`에서 추출한 핵심 WebAuthn 클라이언트 데이터입니다. */
public final class WebAuthnClientData {

	private final String type;
	private final String challenge;
	private final String origin;
	private final boolean crossOrigin;

	public WebAuthnClientData(String type, String challenge, String origin, boolean crossOrigin) {
		this.type = Strings.requireNonBlank(type, "type").trim();
		this.challenge = Strings.requireNonBlank(challenge, "challenge").trim();
		this.origin = Strings.requireNonBlank(origin, "origin").trim();
		this.crossOrigin = crossOrigin;
	}

	public String type() {
		return type;
	}

	public String challenge() {
		return challenge;
	}

	public String origin() {
		return origin;
	}

	public boolean crossOrigin() {
		return crossOrigin;
	}
}
