package com.auth.otp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class Sha256RecoveryCodeVerifierTest {

	@Test
	void verifiesNormalizedRecoveryCodeAgainstStoredHash() {
		Sha256RecoveryCodeVerifier verifier = new Sha256RecoveryCodeVerifier();
		String stored = verifier.hash("ABCD-EFGH");

		assertThat(verifier.verify("abcd efgh", com.auth.core.utils.CollectionUtils.listOf(stored))).isTrue();
		assertThat(verifier.verify("wrong-code", com.auth.core.utils.CollectionUtils.listOf(stored))).isFalse();
	}
}
