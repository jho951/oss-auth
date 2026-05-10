package com.auth.mfa;

import static org.assertj.core.api.Assertions.assertThat;

import com.auth.otp.OtpHashAlgorithm;
import com.auth.otp.TotpVerifier;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TotpMfaVerifierTest {

	private static final String SECRET = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";

	@Test
	void 등록된시크릿과제출코드가일치하면성공한다() {
		TotpVerifier totpVerifier = new TotpVerifier(
			Duration.ofSeconds(30),
			0,
			8,
			OtpHashAlgorithm.SHA1,
			Clock.fixed(Instant.ofEpochSecond(59), ZoneOffset.UTC)
		);
		TotpMfaVerifier verifier = new TotpMfaVerifier(totpVerifier);

		var result = verifier.verify(
			new MfaVerificationRequest(
				new com.auth.core.api.model.Principal("user-1"),
				"totp-1",
				MfaFactorType.TOTP,
				MfaChallengeContext.empty(),
				Map.of("code", "94287082")
			),
			new MfaEnrollment("totp-1", MfaFactorType.TOTP, Instant.parse("2026-01-01T00:00:00Z"), Map.of("otp_secret", SECRET))
		);

		assertThat(result).isPresent();
		assertThat(result.orElseThrow()).containsEntry("verified_by", "totp");
	}
}
