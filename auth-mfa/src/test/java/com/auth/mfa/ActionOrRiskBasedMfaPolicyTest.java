package com.auth.mfa;

import static org.assertj.core.api.Assertions.assertThat;

import com.auth.core.api.model.Principal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ActionOrRiskBasedMfaPolicyTest {

	@Test
	void requiresMfaForProtectedAction() {
		ActionOrRiskBasedMfaPolicy policy = new ActionOrRiskBasedMfaPolicy(
			List.of("wire-transfer"),
			null,
			List.of(MfaFactorType.PASSKEY, MfaFactorType.TOTP),
			null
		);

		MfaRequirement requirement = policy.evaluate(
			new Principal("user-1"),
			new MfaChallengeContext("wire-transfer", MfaRiskLevel.LOW, Map.of()),
			List.of(
				new MfaEnrollment("passkey-1", MfaFactorType.PASSKEY, Instant.parse("2026-01-01T00:00:00Z"), Map.of()),
				new MfaEnrollment("totp-1", MfaFactorType.TOTP, Instant.parse("2026-01-01T00:00:00Z"), Map.of())
			)
		);

		assertThat(requirement.isRequired()).isTrue();
		assertThat(requirement.getAcceptableFactors()).containsExactly(MfaFactorType.PASSKEY, MfaFactorType.TOTP);
		assertThat(requirement.getReason()).hasValueSatisfying(value -> assertThat(value).contains("protected action"));
	}

	@Test
	void requiresMfaForElevatedRiskEvenWithoutProtectedAction() {
		ActionOrRiskBasedMfaPolicy policy = new ActionOrRiskBasedMfaPolicy(
			List.of(),
			MfaRiskLevel.HIGH,
			List.of(),
			null
		);

		MfaRequirement requirement = policy.evaluate(
			new Principal("user-1"),
			new MfaChallengeContext("view-profile", MfaRiskLevel.CRITICAL, Map.of()),
			List.of(new MfaEnrollment("otp-1", MfaFactorType.OTP, Instant.parse("2026-01-01T00:00:00Z"), Map.of()))
		);

		assertThat(requirement.isRequired()).isTrue();
		assertThat(requirement.getAcceptableFactors()).containsExactly(MfaFactorType.OTP);
		assertThat(requirement.getReason()).hasValueSatisfying(value -> assertThat(value).contains("elevated risk"));
	}

	@Test
	void skipsMfaWhenActionAndRiskDoNotMatch() {
		ActionOrRiskBasedMfaPolicy policy = new ActionOrRiskBasedMfaPolicy(
			List.of("wire-transfer"),
			MfaRiskLevel.HIGH,
			List.of(MfaFactorType.TOTP),
			null
		);

		MfaRequirement requirement = policy.evaluate(
			new Principal("user-1"),
			new MfaChallengeContext("view-profile", MfaRiskLevel.LOW, Map.of()),
			List.of(new MfaEnrollment("totp-1", MfaFactorType.TOTP, Instant.parse("2026-01-01T00:00:00Z"), Map.of()))
		);

		assertThat(requirement.isRequired()).isFalse();
	}
}
