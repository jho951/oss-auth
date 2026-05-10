package com.auth.otp;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class TotpVerifierTest {

	private static final String SECRET_SHA1 = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";

	@Test
	void matchesRfc6238VectorForSha1() {
		TotpVerifier verifier = new TotpVerifier(
			Duration.ofSeconds(30),
			0,
			8,
			OtpHashAlgorithm.SHA1,
			Clock.fixed(Instant.ofEpochSecond(59), ZoneOffset.UTC)
		);

		assertThat(verifier.generate(SECRET_SHA1, Instant.ofEpochSecond(59))).isEqualTo("94287082");
		assertThat(verifier.verify(SECRET_SHA1, Instant.ofEpochSecond(59), "94287082")).isTrue();
	}
}
