package com.auth.otp;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

/** SHA-256 기반 복구 코드 해시 및 검증 도우미입니다. */
public final class Sha256RecoveryCodeVerifier {

	public String hash(String code) {
		String normalized = normalize(code);
		if (normalized.isBlank()) throw new IllegalArgumentException("code must not be blank");
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(normalized.getBytes(StandardCharsets.UTF_8)));
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
}
