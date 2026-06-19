package com.auth.otp;

import com.auth.core.utils.Strings;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** SHA-256 기반 복구 코드 해시 및 검증 도우미입니다. */
public final class Sha256RecoveryCodeVerifier {

	public String hash(String code) {
		String normalized = normalize(code);
		if (Strings.isBlank(normalized)) throw new IllegalArgumentException("code must not be blank");
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return toHex(digest.digest(normalized.getBytes(StandardCharsets.UTF_8)));
		} catch (Exception e) {
			throw new IllegalStateException("failed to hash recovery code", e);
		}
	}

	public boolean verify(String code, Iterable<String> storedHashes) {
		if (storedHashes == null) return false;
		String candidate = hash(code);
		for (String storedHash : storedHashes) {
			if (candidate.equalsIgnoreCase(normalize(storedHash))) {
				return true;
			}
		}
		return false;
	}

	private static String normalize(String value) {
		return value == null ? "" : value.replace("-", "").replace(" ", "").trim().toLowerCase();
	}

	private static String toHex(byte[] bytes) {
		char[] hex = new char[bytes.length * 2];
		char[] alphabet = "0123456789abcdef".toCharArray();
		for (int i = 0; i < bytes.length; i++) {
			int value = bytes[i] & 0xFF;
			hex[i * 2] = alphabet[value >>> 4];
			hex[i * 2 + 1] = alphabet[value & 0x0F];
		}
		return new String(hex);
	}
}
