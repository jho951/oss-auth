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
		Principal principal = new Principal("user-1", List.of("USER"), Map.of("tenant_id", "tenant-a"));
		MfaEnrollment enrollment = new MfaEnrollment("totp-1", MfaFactorType.TOTP, Instant.parse("2026-01-01T00:00:00Z"), Map.of());
		MfaService service = new MfaService(
			ignored -> List.of(enrollment),
			new NeverRequireMfaPolicy(),
			List.of(new TestTotpVerifier()),
			new DefaultMfaPrincipalMapper(Clock.fixed(Instant.parse("2026-03-01T10:15:30Z"), ZoneOffset.UTC))
		);

		Optional<Principal> result = service.stepUp(new MfaVerificationRequest(
			principal,
			"totp-1",
			MfaFactorType.TOTP,
			new MfaChallengeContext("wire-transfer", MfaRiskLevel.HIGH, Map.of()),
			Map.of("code", "123456")
		));

		assertThat(result).isPresent();
		assertThat(result.orElseThrow().getAttribute("mfa_authenticated")).isEqualTo(true);
		assertThat(result.orElseThrow().getAttribute("mfa_factor_type")).isEqualTo("totp");
		assertThat(result.orElseThrow().getAttribute("mfa_action")).isEqualTo("wire-transfer");
		assertThat(result.orElseThrow().getAttribute("mfa_authenticated_at"))
			.isEqualTo(Instant.parse("2026-03-01T10:15:30Z"));
		assertThat(result.orElseThrow().getAttribute("verified_by")).isEqualTo("test-totp");
	}

	@Test
	void stepUpReturnsEmptyWhenEnrollmentIsAmbiguous() {
		Principal principal = new Principal("user-1");
		MfaService service = new MfaService(
			ignored -> List.of(
				new MfaEnrollment("totp-1", MfaFactorType.TOTP, Instant.parse("2026-01-01T00:00:00Z"), Map.of()),
				new MfaEnrollment("totp-2", MfaFactorType.TOTP, Instant.parse("2026-01-01T00:00:00Z"), Map.of())
			),
			new NeverRequireMfaPolicy(),
			List.of(new TestTotpVerifier()),
			new DefaultMfaPrincipalMapper()
		);

		Optional<Principal> result = service.stepUp(new MfaVerificationRequest(
			principal,
			null,
			MfaFactorType.TOTP,
			MfaChallengeContext.empty(),
			Map.of("code", "123456")
		));

		assertThat(result).isEmpty();
	}

	@Test
	void evaluateDelegatesToPolicyWithResolvedEnrollments() {
		Principal principal = new Principal("user-1");
		MfaService service = new MfaService(
			ignored -> List.of(new MfaEnrollment("passkey-1", MfaFactorType.PASSKEY, Instant.parse("2026-01-01T00:00:00Z"), Map.of())),
			new ActionOrRiskBasedMfaPolicy(List.of("delete-tenant"), null, List.of(MfaFactorType.PASSKEY), null),
			List.of(),
			new DefaultMfaPrincipalMapper()
		);

		MfaRequirement result = service.evaluate(
			principal,
			new MfaChallengeContext("delete-tenant", MfaRiskLevel.LOW, Map.of())
		);

		assertThat(result.isRequired()).isTrue();
		assertThat(result.getAcceptableFactors()).containsExactly(MfaFactorType.PASSKEY);
	}

	@Test
	void stepUpRejectsFactorOutsidePolicy() {
		Principal principal = new Principal("user-1");
		MfaService service = new MfaService(
			ignored -> List.of(new MfaEnrollment("otp-1", MfaFactorType.OTP, Instant.parse("2026-01-01T00:00:00Z"), Map.of())),
			new ActionOrRiskBasedMfaPolicy(List.of("wire-transfer"), null, List.of(MfaFactorType.PASSKEY), null),
			List.of(new OtpVerifier()),
			new DefaultMfaPrincipalMapper()
		);

		Optional<Principal> result = service.stepUp(new MfaVerificationRequest(
			principal,
			"otp-1",
			MfaFactorType.OTP,
			new MfaChallengeContext("wire-transfer", MfaRiskLevel.LOW, Map.of()),
			Map.of("code", "654321")
		));

		assertThat(result).isEmpty();
	}

	@Test
	void stepUpWorksWithRealTotpVerifierIntegration() {
		Principal principal = new Principal("user-1", List.of("USER"), Map.of());
		MfaEnrollment enrollment = new MfaEnrollment(
			"totp-1",
			MfaFactorType.TOTP,
			Instant.parse("2026-01-01T00:00:00Z"),
			Map.of("otp_secret", "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ")
		);
		TotpVerifier totpVerifier = new TotpVerifier(
			Duration.ofSeconds(30),
			0,
			8,
			OtpHashAlgorithm.SHA1,
			Clock.fixed(Instant.ofEpochSecond(59), ZoneOffset.UTC)
		);
		MfaService service = new MfaService(
			ignored -> List.of(enrollment),
			new NeverRequireMfaPolicy(),
			List.of(new TotpMfaVerifier(totpVerifier)),
			new DefaultMfaPrincipalMapper(Clock.fixed(Instant.parse("2026-03-01T10:15:30Z"), ZoneOffset.UTC))
		);

		Optional<Principal> result = service.stepUp(new MfaVerificationRequest(
			principal,
			"totp-1",
			MfaFactorType.TOTP,
			MfaChallengeContext.empty(),
			Map.of("code", "94287082")
		));

		assertThat(result).isPresent();
		assertThat(result.orElseThrow().getAttribute("verified_by")).isEqualTo("totp");
		assertThat(result.orElseThrow().getAttribute("mfa_factor_type")).isEqualTo("totp");
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
				.map(ignored -> Map.<String, Object>of("verified_by", "test-totp"));
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
				.map(ignored -> Map.<String, Object>of());
		}
	}
}
