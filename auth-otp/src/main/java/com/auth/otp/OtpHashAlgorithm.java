package com.auth.otp;

/** HOTP/TOTP 생성에 사용할 수 있는 HMAC 알고리즘 목록입니다. */
public enum OtpHashAlgorithm {
	SHA1("HmacSHA1"),
	SHA256("HmacSHA256"),
	SHA512("HmacSHA512");

	private final String macAlgorithm;

	OtpHashAlgorithm(String macAlgorithm) {
		this.macAlgorithm = macAlgorithm;
	}

	public String macAlgorithm() {
		return macAlgorithm;
	}
}
