package com.auth.saml;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.auth.core.api.exception.AuthException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DefaultSamlAssertionValidatorTest {

	@Test
	void acceptsValidAudienceAndTimeWindow() {
		DefaultSamlAssertionValidator validator = new DefaultSamlAssertionValidator(
			Clock.fixed(Instant.parse("2026-01-01T00:00:30Z"), ZoneOffset.UTC),
			Duration.ofSeconds(30)
		);

		assertThatCode(() -> validator.validate(
			new SamlAssertion(
				"user-1",
				"https://idp.example.com",
				com.auth.core.utils.CollectionUtils.listOf("urn:test:sp"),
				"https://sp.example.com/acs",
				"req-1",
				"session-1",
				Instant.parse("2026-01-01T00:00:00Z"),
				Instant.parse("2026-01-01T00:00:00Z"),
				Instant.parse("2026-01-01T00:05:00Z"),
				com.auth.core.utils.CollectionUtils.mapOf()
			),
			new SamlAuthenticationRequest("<xml/>", "urn:test:sp", "https://sp.example.com/acs", "req-1", com.auth.core.utils.CollectionUtils.mapOf())
		)).doesNotThrowAnyException();
	}

	@Test
	void rejectsWrongAudience() {
		DefaultSamlAssertionValidator validator = new DefaultSamlAssertionValidator();

		assertThatThrownBy(() -> validator.validate(
			new SamlAssertion("user-1", "issuer", com.auth.core.utils.CollectionUtils.listOf("urn:other"), "", "", "", null, null, null, com.auth.core.utils.CollectionUtils.mapOf()),
			new SamlAuthenticationRequest("<xml/>", "urn:test:sp", "", "", com.auth.core.utils.CollectionUtils.mapOf())
		)).isInstanceOf(AuthException.class);
	}
}
