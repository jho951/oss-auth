package com.auth.webauthn;

import com.auth.core.utils.Strings;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;

/** Base64URL 인코딩된 authenticator data를 해석하는 기본 파서입니다. */
public final class DefaultWebAuthnAuthenticatorDataParser implements WebAuthnAuthenticatorDataParser {

	private static final int MIN_AUTHENTICATOR_DATA_LENGTH = 37;
	private static final int USER_PRESENT_FLAG = 0x01;
	private static final int USER_VERIFIED_FLAG = 0x04;

	@Override
	public WebAuthnAuthenticatorData parse(String authenticatorData) {
		if (Strings.isBlank(authenticatorData)) throw new IllegalArgumentException("authenticatorData must not be blank");
		byte[] rawBytes = Base64.getUrlDecoder().decode(authenticatorData);
		if (rawBytes.length < MIN_AUTHENTICATOR_DATA_LENGTH) {
			throw new IllegalArgumentException("authenticatorData must be at least 37 bytes");
		}
		byte[] rpIdHash = Arrays.copyOfRange(rawBytes, 0, 32);
		int flags = rawBytes[32] & 0xFF;
		long signCount = Integer.toUnsignedLong(ByteBuffer.wrap(rawBytes, 33, 4).getInt());
		return new WebAuthnAuthenticatorData(
			rawBytes,
			rpIdHash,
			(flags & USER_PRESENT_FLAG) != 0,
			(flags & USER_VERIFIED_FLAG) != 0,
			signCount
		);
	}
}
