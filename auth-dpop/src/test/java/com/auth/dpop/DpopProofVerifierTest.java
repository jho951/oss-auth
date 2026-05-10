package com.auth.dpop;

import static org.assertj.core.api.Assertions.assertThat;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DpopProofVerifierTest {

	@Test
	void verifiesSignedProofAndBindsAccessToken() throws Exception {
		KeyPair keyPair = ecKeyPair();
		String accessToken = "access-token";
		Instant issuedAt = Instant.parse("2026-01-01T00:00:00Z");
		String proof = Jwts.builder()
			.setHeaderParam("typ", "dpop+jwt")
			.setHeaderParam("jwk", DpopTokenBindingHelper.publicJwk(keyPair.getPublic(), "dpop-1"))
			.setId("proof-1")
			.claim("htm", "GET")
			.claim("htu", "https://api.example.com/resource")
			.claim("ath", DpopTokenBindingHelper.accessTokenHash(accessToken))
			.setIssuedAt(Date.from(issuedAt))
			.signWith(keyPair.getPrivate(), SignatureAlgorithm.ES256)
			.compact();

		DpopProofVerifier verifier = new DpopProofVerifier(
			Clock.fixed(issuedAt.plusSeconds(30), ZoneOffset.UTC),
			Duration.ofMinutes(5),
			new InMemoryDpopReplayValidator(Clock.fixed(issuedAt.plusSeconds(30), ZoneOffset.UTC))
		);

		var result = verifier.verify(new DpopProofRequest(
			proof,
			"GET",
			"https://api.example.com/resource",
			accessToken,
			""
		));

		assertThat(result).isPresent();
		assertThat(result.orElseThrow().getTokenId()).isEqualTo("proof-1");
		assertThat(result.orElseThrow().getMethod()).isEqualTo("GET");
	}

	@Test
	void rejectsReplayOfSameProof() throws Exception {
		KeyPair keyPair = ecKeyPair();
		Instant issuedAt = Instant.parse("2026-01-01T00:00:00Z");
		String proof = Jwts.builder()
			.setHeaderParam("typ", "dpop+jwt")
			.setHeaderParam("jwk", DpopTokenBindingHelper.publicJwk(keyPair.getPublic(), "dpop-1"))
			.setId("proof-1")
			.claim("htm", "POST")
			.claim("htu", "https://api.example.com/resource")
			.setIssuedAt(Date.from(issuedAt))
			.signWith(keyPair.getPrivate(), SignatureAlgorithm.ES256)
			.compact();

		Clock clock = Clock.fixed(issuedAt.plusSeconds(10), ZoneOffset.UTC);
		DpopReplayValidator replayValidator = new InMemoryDpopReplayValidator(clock);
		DpopProofVerifier verifier = new DpopProofVerifier(clock, Duration.ofMinutes(5), replayValidator);

		assertThat(verifier.verify(new DpopProofRequest(proof, "POST", "https://api.example.com/resource", "", ""))).isPresent();
		assertThat(verifier.verify(new DpopProofRequest(proof, "POST", "https://api.example.com/resource", "", ""))).isEmpty();
	}

	private static KeyPair ecKeyPair() throws Exception {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
		generator.initialize(256);
		return generator.generateKeyPair();
	}
}
