package com.auth.otp;

import java.nio.ByteBuffer;
import java.util.Locale;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** RFC 4226 기반 HOTP 생성 및 검증기입니다. */
public final class HotpVerifier {

	private final int digits;
	private final OtpHashAlgorithm algorithm;

	public HotpVerifier() {
		this(6, OtpHashAlgorithm.SHA1);
	}

	public HotpVerifier(int digits, OtpHashAlgorithm algorithm) {
		if (digits < 6 || digits > 10) throw new IllegalArgumentException("digits must be between 6 and 10");
		this.digits = digits;
		this.algorithm = algorithm == null ? OtpHashAlgorithm.SHA1 : algorithm;
	}

	public boolean verify(String base32Secret, long counter, String code) {
		return generate(base32Secret, counter).equals(normalizeCode(code));
	}

	public String generate(String base32Secret, long counter) {
		return generate(Base32SecretCodec.decode(base32Secret), counter);
	}

	public String generate(byte[] secret, long counter) {
		try {
			Mac mac = Mac.getInstance(algorithm.macAlgorithm());
			mac.init(new SecretKeySpec(secret, algorithm.macAlgorithm()));
			byte[] hmac = mac.doFinal(ByteBuffer.allocate(8).putLong(counter).array());
			int offset = hmac[hmac.length - 1] & 0x0F;
			int binary = ((hmac[offset] & 0x7F) << 24)
				| ((hmac[offset + 1] & 0xFF) << 16)
				| ((hmac[offset + 2] & 0xFF) << 8)
				| (hmac[offset + 3] & 0xFF);
			int otp = binary % (int) Math.pow(10, digits);
			return String.format(Locale.ROOT, "%0" + digits + "d", otp);
		} catch (Exception e) {
			throw new IllegalStateException("failed to generate HOTP", e);
		}
	}

	private String normalizeCode(String code) {
		if (code == null) return "";
		return code.trim();
	}
}
