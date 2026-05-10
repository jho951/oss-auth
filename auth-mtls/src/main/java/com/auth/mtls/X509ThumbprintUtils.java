package com.auth.mtls;

import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Map;

/** x5t#S256 지문을 계산하고 인증서 바운드 토큰 확인 claim을 검증합니다. */
public final class X509ThumbprintUtils {

	private X509ThumbprintUtils() {
	}

	public static String sha256Thumbprint(X509Certificate certificate) {
		if (certificate == null) throw new IllegalArgumentException("certificate must not be null");
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return Base64.getUrlEncoder().withoutPadding().encodeToString(digest.digest(certificate.getEncoded()));
		} catch (Exception e) {
			throw new IllegalStateException("failed to compute certificate thumbprint", e);
		}
	}

	public static boolean matchesConfirmation(Map<String, Object> claims, X509Certificate certificate) {
		if (claims == null || claims.isEmpty() || certificate == null) return false;
		Object cnf = claims.get("cnf");
		if (!(cnf instanceof Map<?, ?> cnfMap)) return false;
		Object thumbprint = cnfMap.get("x5t#S256");
		return thumbprint != null && sha256Thumbprint(certificate).equals(String.valueOf(thumbprint));
	}
}
