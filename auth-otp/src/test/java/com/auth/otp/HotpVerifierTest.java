package com.auth.otp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HotpVerifierTest {

	private static final String SECRET = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";

	@Test
	void matchesRfc4226Vectors() {
		HotpVerifier verifier = new HotpVerifier(6, OtpHashAlgorithm.SHA1);

		assertThat(verifier.generate(SECRET, 0)).isEqualTo("755224");
		assertThat(verifier.generate(SECRET, 1)).isEqualTo("287082");
		assertThat(verifier.generate(SECRET, 2)).isEqualTo("359152");
		assertThat(verifier.generate(SECRET, 3)).isEqualTo("969429");
		assertThat(verifier.generate(SECRET, 4)).isEqualTo("338314");
		assertThat(verifier.generate(SECRET, 5)).isEqualTo("254676");
		assertThat(verifier.generate(SECRET, 6)).isEqualTo("287922");
		assertThat(verifier.generate(SECRET, 7)).isEqualTo("162583");
		assertThat(verifier.generate(SECRET, 8)).isEqualTo("399871");
		assertThat(verifier.generate(SECRET, 9)).isEqualTo("520489");
	}
}
