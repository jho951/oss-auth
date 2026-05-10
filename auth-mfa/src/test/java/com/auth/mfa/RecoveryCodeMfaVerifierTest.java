package com.auth.mfa;

import static org.assertj.core.api.Assertions.assertThat;

import com.auth.otp.Sha256RecoveryCodeVerifier;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RecoveryCodeMfaVerifierTest {

	@Test
	void 저장된해시와일치하는리커버리코드를검증한다() {
		Sha256RecoveryCodeVerifier hashVerifier = new Sha256RecoveryCodeVerifier();
		RecoveryCodeMfaVerifier verifier = new RecoveryCodeMfaVerifier(hashVerifier);
		String storedHash = hashVerifier.hash("ABCD-EFGH");

		var result = verifier.verify(
			new MfaVerificationRequest(
				new com.auth.core.api.model.Principal("user-1"),
				"rc-1",
				MfaFactorType.RECOVERY_CODE,
				MfaChallengeContext.empty(),
				Map.of("code", "abcd efgh")
			),
			new MfaEnrollment(
				"rc-1",
				MfaFactorType.RECOVERY_CODE,
				Instant.parse("2026-01-01T00:00:00Z"),
				Map.of("recovery_hashes", List.of(storedHash))
			)
		);

		assertThat(result).isPresent();
		assertThat(result.orElseThrow()).containsEntry("verified_by", "recovery_code");
		assertThat(result.orElseThrow().get("matched_recovery_code_hash")).isEqualTo(storedHash);
	}
}
