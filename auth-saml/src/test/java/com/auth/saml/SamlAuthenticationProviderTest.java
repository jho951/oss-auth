package com.auth.saml;

import static org.assertj.core.api.Assertions.assertThat;

import com.auth.core.api.model.Principal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class SamlAuthenticationProviderTest {

	@Test
	void authenticatesMappedSamlAssertion() {
		SamlAuthenticationProvider provider = new SamlAuthenticationProvider(
			request -> new SamlAssertion(
				"user-1",
				"https://idp.example.com",
				List.of("urn:test:sp"),
				"https://sp.example.com/acs",
				"req-1",
				"session-1",
				Instant.parse("2026-01-01T00:00:00Z"),
				Instant.parse("2025-12-31T23:59:00Z"),
				Instant.parse("2026-01-01T01:00:00Z"),
				Map.of("email", "user@example.com")
			),
			new DefaultSamlAssertionValidator(
				Clock.fixed(Instant.parse("2026-01-01T00:10:00Z"), ZoneOffset.UTC),
				java.time.Duration.ofMinutes(2)
			),
			assertion -> new Principal(assertion.getSubject(), List.of("USER"), assertion.getAttributes())
		);

		Optional<Principal> result = provider.authenticate(
			new SamlAuthenticationRequest("<xml/>", "urn:test:sp", "https://sp.example.com/acs", "req-1", Map.of())
		);

		assertThat(result).isPresent();
		assertThat(result.orElseThrow().getUserId()).isEqualTo("user-1");
	}
}
