package com.auth.otp;

import com.auth.core.utils.Strings;
import java.io.ByteArrayOutputStream;

/** OTP 공유 비밀값을 복호화하기 위한 최소한의 RFC 4648 Base32 디코더입니다. */
public final class Base32SecretCodec {

	private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

	private Base32SecretCodec() {
	}

	public static byte[] decode(String value) {
		if (Strings.isBlank(value)) throw new IllegalArgumentException("value must not be blank");
		String normalized = value.replace("=", "").replace(" ", "").toUpperCase();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int buffer = 0;
		int bitsLeft = 0;
		for (int i = 0; i < normalized.length(); i++) {
			int index = ALPHABET.indexOf(normalized.charAt(i));
			if (index < 0) {
				throw new IllegalArgumentException("invalid base32 character: " + normalized.charAt(i));
			}
			buffer = (buffer << 5) | index;
			bitsLeft += 5;
			if (bitsLeft >= 8) {
				out.write((buffer >> (bitsLeft - 8)) & 0xFF);
				bitsLeft -= 8;
			}
		}
		return out.toByteArray();
	}
}
