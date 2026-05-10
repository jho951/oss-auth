package com.auth.saml;

import com.auth.core.api.exception.AuthException;
import com.auth.core.api.exception.AuthFailureReason;
import com.auth.core.utils.Strings;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/** SAML audience, recipient, 시간 조건, 요청 상관관계를 검증하는 기본 validator입니다. */
public final class DefaultSamlAssertionValidator implements SamlAssertionValidator {

	private final Clock clock;
	private final Duration allowedClockSkew;

	public DefaultSamlAssertionValidator() {
		this(Clock.systemUTC(), Duration.ofMinutes(2));
	}

	public DefaultSamlAssertionValidator(Clock clock, Duration allowedClockSkew) {
		this.clock = Objects.requireNonNull(clock, "clock");
		this.allowedClockSkew = allowedClockSkew == null ? Duration.ZERO : allowedClockSkew;
	}

	@Override
	public void validate(SamlAssertion assertion, SamlAuthenticationRequest request) {
		Objects.requireNonNull(assertion, "assertion");
		Objects.requireNonNull(request, "request");
		Instant now = Instant.now(clock);

		if (!Strings.isBlank(request.getExpectedAudience())
			&& assertion.getAudiences().stream().noneMatch(request.getExpectedAudience()::equals)) {
			throw new AuthException(AuthFailureReason.INVALID_TOKEN, "invalid SAML audience");
		}

		if (!Strings.isBlank(request.getExpectedRecipient())
			&& !request.getExpectedRecipient().equals(assertion.getRecipient())) {
			throw new AuthException(AuthFailureReason.INVALID_TOKEN, "invalid SAML recipient");
		}

		if (!Strings.isBlank(request.getRequestId())
			&& !request.getRequestId().equals(assertion.getInResponseTo())) {
			throw new AuthException(AuthFailureReason.INVALID_TOKEN, "invalid SAML inResponseTo");
		}

		if (assertion.getNotBefore() != null && now.isBefore(assertion.getNotBefore().minus(allowedClockSkew))) {
			throw new AuthException(AuthFailureReason.INVALID_TOKEN, "SAML assertion not yet valid");
		}

		if (assertion.getNotOnOrAfter() != null && !now.isBefore(assertion.getNotOnOrAfter().plus(allowedClockSkew))) {
			throw new AuthException(AuthFailureReason.INVALID_TOKEN, "SAML assertion expired");
		}
	}
}
