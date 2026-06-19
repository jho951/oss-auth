package com.auth.mfa;

import static org.assertj.core.api.Assertions.assertThat;

import com.auth.core.api.model.Principal;
import com.auth.otp.OtpHashAlgorithm;
import com.auth.otp.TotpVerifier;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class MfaServiceTest {

	@Test
	void stepUpAddsMfaAttributesWhenVerificationSucceeds() {
		Principal principal = new Principal("user-1", com.auth.core.utils.CollectionUtils.listOf("USER"), com.auth.core.utils.CollectionUtils.mapOf("tenant_id", "tenant-a"));
		MfaEnrollment enrollment = new MfaEnrollment("totp-1", MfaFactorType.TOTP, Instant.parse("2026-01-01T00:00:00Z"), com.auth.core.utils.CollectionUtils.mapOf());
		MfaService service = new MfaService(
			ignored -> com.auth.core.utils.CollectionUtils.listOf(enrollment),
			new NeverRequireMfaPolicy(),
			com.auth.core.utils.CollectionUtils.listOf(new TestTotpVerifier()),
			new DefaultMfaPrincipalMapper(Clock.fixed(Instant.parse("2026-03-01T10:15:30Z"), ZoneOffset.UTC))
		);

		Optional<Principal> result = service.stepUp(new MfaVerificationRequest(
			principal,
			"totp-1",
			MfaFactorType.TOTP,
			new MfaChallengeContext("wire-transfer", MfaRiskLevel.HIGH, com.auth.core.utils.CollectionUtils.mapOf()),
			com.auth.core.utils.CollectionUtils.mapOf("code", "123456")
		));

		assertThat(result).isPresent();
		assertThat(result.get().getAttribute("mfa_authenticated")).isEqualTo(true);
		assertThat(result.get().getAttribute("mfa_factor_type")).isEqualTo("totp");
		assertThat(result.get().getAttribute("mfa_action")).isEqualTo("wire-transfer");
		assertThat(result.get().getAttribute("mfa_authenticated_at"))
			.isEqualTo(Instant.parse("2026-03-01T10:15:30Z"));
		assertThat(result.get().getAttribute("verified_by")).isEqualTo("test-totp");
	}

	@Test
	void stepUpReturnsEmptyWhenEnrollmentIsAmbiguous() {
		Principal principal = new Principal("user-1");
		MfaService service = new MfaService(
			ignored -> com.auth.core.utils.CollectionUtils.listOf(
				new MfaEnrollment("totp-1", MfaFactorType.TOTP, Instant.parse("2026-01-01T00:00:00Z"), com.auth.core.utils.CollectionUtils.mapOf()),
				new MfaEnrollment("totp-2", MfaFactorType.TOTP, Instant.parse("2026-01-01T00:00:00Z"), com.auth.core.utils.CollectionUtils.mapOf())
			),
			new NeverRequireMfaPolicy(),
			com.auth.core.utils.CollectionUtils.listOf(new TestTotpVerifier()),
			new DefaultMfaPrincipalMapper()
		);

		Optional<Principal> result = service.stepUp(new MfaVerificationRequest(
			principal,
			null,
			MfaFactorType.TOTP,
			MfaChallengeContext.empty(),
			com.auth.core.utils.CollectionUtils.mapOf("code", "123456")
		));

		assertThat(result).isEmpty();
	}

	@Test
	void evaluateDelegatesToPolicyWithResolvedEnrollments() {
		Principal principal = new Principal("user-1");
		MfaService service = new MfaService(
			ignored -> com.auth.core.utils.CollectionUtils.listOf(new MfaEnrollment("passkey-1", MfaFactorType.PASSKEY, Instant.parse("2026-01-01T00:00:00Z"), com.auth.core.utils.CollectionUtils.mapOf())),
			new ActionOrRiskBasedMfaPolicy(com.auth.core.utils.CollectionUtils.listOf("delete-tenant"), null, com.auth.core.utils.CollectionUtils.listOf(MfaFactorType.PASSKEY), null),
			com.auth.core.utils.CollectionUtils.listOf(),
			new DefaultMfaPrincipalMapper()
		);

		MfaRequirement result = service.evaluate(
			principal,
			new MfaChallengeContext("delete-tenant", MfaRiskLevel.LOW, com.auth.core.utils.CollectionUtils.mapOf())
		);

		assertThat(result.isRequired()).isTrue();
		assertThat(result.getAcceptableFactors()).containsExactly(MfaFactorType.PASSKEY);
	}

	@Test
	void stepUpRejectsFactorOutsidePolicy() {
		Principal principal = new Principal("user-1");
		MfaService service = new MfaService(
			ignored -> com.auth.core.utils.CollectionUtils.listOf(new MfaEnrollment("otp-1", MfaFactorType.OTP, Instant.parse("2026-01-01T00:00:00Z"), com.auth.core.utils.CollectionUtils.mapOf())),
			new ActionOrRiskBasedMfaPolicy(com.auth.core.utils.CollectionUtils.listOf("wire-transfer"), null, com.auth.core.utils.CollectionUtils.listOf(MfaFactorType.PASSKEY), null),
			com.auth.core.utils.CollectionUtils.listOf(new OtpVerifier()),
			new DefaultMfaPrincipalMapper()
		);

		Optional<Principal> result = service.stepUp(new MfaVerificationRequest(
			principal,
			"otp-1",
			MfaFactorType.OTP,
			new MfaChallengeContext("wire-transfer", MfaRiskLevel.LOW, com.auth.core.utils.CollectionUtils.mapOf()),
			com.auth.core.utils.CollectionUtils.mapOf("code", "654321")
		));

		assertThat(result).isEmpty();
	}

	@Test
	void stepUpWorksWithRealTotpVerifierIntegration() {
		Principal principal = new Principal("user-1", com.auth.core.utils.CollectionUtils.listOf("USER"), com.auth.core.utils.CollectionUtils.mapOf());
		MfaEnrollment enrollment = new MfaEnrollment(
			"totp-1",
			MfaFactorType.TOTP,
			Instant.parse("2026-01-01T00:00:00Z"),
			com.auth.core.utils.CollectionUtils.mapOf("otp_secret", "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ")
		);
		TotpVerifier totpVerifier = new TotpVerifier(
			Duration.ofSeconds(30),
			0,
			8,
			OtpHashAlgorithm.SHA1,
			Clock.fixed(Instant.ofEpochSecond(59), ZoneOffset.UTC)
		);
		MfaService service = new MfaService(
			ignored -> com.auth.core.utils.CollectionUtils.listOf(enrollment),
			new NeverRequireMfaPolicy(),
			com.auth.core.utils.CollectionUtils.listOf(new TotpMfaVerifier(totpVerifier)),
			new DefaultMfaPrincipalMapper(Clock.fixed(Instant.parse("2026-03-01T10:15:30Z"), ZoneOffset.UTC))
		);

		Optional<Principal> result = service.stepUp(new MfaVerificationRequest(
			principal,
			"totp-1",
			MfaFactorType.TOTP,
			MfaChallengeContext.empty(),
			com.auth.core.utils.CollectionUtils.mapOf("code", "94287082")
		));

		assertThat(result).isPresent();
		assertThat(result.get().getAttribute("verified_by")).isEqualTo("totp");
		assertThat(result.get().getAttribute("mfa_factor_type")).isEqualTo("totp");
	}

	private static final class TestTotpVerifier implements MfaVerifier {
		@Override
		public boolean supports(MfaFactorType factorType) {
			return factorType == MfaFactorType.TOTP;
		}

		@Override
		public Optional<Map<String, Object>> verify(MfaVerificationRequest request, MfaEnrollment enrollment) {
			return request.getProof("code")
				.filter("123456"::equals)
				.map(ignored -> com.auth.core.utils.CollectionUtils.<String, Object>mapOf("verified_by", "test-totp"));
		}
	}

	private static final class OtpVerifier implements MfaVerifier {
		@Override
		public boolean supports(MfaFactorType factorType) {
			return factorType == MfaFactorType.OTP;
		}

		@Override
		public Optional<Map<String, Object>> verify(MfaVerificationRequest request, MfaEnrollment enrollment) {
			return request.getProof("code")
				.filter("654321"::equals)
				.map(ignored -> com.auth.core.utils.CollectionUtils.<String, Object>mapOf());
		}
	}
}
