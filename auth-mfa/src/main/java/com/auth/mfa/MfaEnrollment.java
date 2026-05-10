package com.auth.mfa;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/** step-up 요구를 만족시킬 수 있는 등록된 2차 인증 수단입니다. */
public final class MfaEnrollment {

	private final String factorId;
	private final MfaFactorType factorType;
	private final Instant enrolledAt;
	private final Map<String, Object> attributes;

	public MfaEnrollment(String factorId, MfaFactorType factorType, Instant enrolledAt, Map<String, Object> attributes) {
		if (factorId == null || factorId.isBlank()) throw new IllegalArgumentException("factorId must not be blank");
		this.factorId = factorId;
		this.factorType = Objects.requireNonNull(factorType, "factorType");
		this.enrolledAt = enrolledAt == null ? Instant.now() : enrolledAt;
		this.attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
	}

	public String getFactorId() {
		return factorId;
	}

	public MfaFactorType getFactorType() {
		return factorType;
	}

	public Instant getEnrolledAt() {
		return enrolledAt;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}
}
