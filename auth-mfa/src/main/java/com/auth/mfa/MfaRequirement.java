package com.auth.mfa;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** step-up 시도 전에 MFA 정책이 반환하는 판단 결과입니다. */
public final class MfaRequirement {

	private static final MfaRequirement NOT_REQUIRED = new MfaRequirement(false, List.of(), null, Map.of());

	private final boolean required;
	private final List<MfaFactorType> acceptableFactors;
	private final String reason;
	private final Map<String, Object> attributes;

	public MfaRequirement(
		boolean required,
		List<MfaFactorType> acceptableFactors,
		String reason,
		Map<String, Object> attributes
	) {
		this.required = required;
		this.acceptableFactors = acceptableFactors == null ? List.of() : List.copyOf(acceptableFactors);
		this.reason = reason;
		this.attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
	}

	public static MfaRequirement notRequired() {
		return NOT_REQUIRED;
	}

	public static MfaRequirement required(List<MfaFactorType> acceptableFactors, String reason) {
		return new MfaRequirement(true, acceptableFactors, reason, Map.of());
	}

	public static MfaRequirement required(List<MfaFactorType> acceptableFactors, String reason, Map<String, Object> attributes) {
		return new MfaRequirement(true, acceptableFactors, reason, attributes);
	}

	public boolean isRequired() {
		return required;
	}

	public List<MfaFactorType> getAcceptableFactors() {
		return acceptableFactors;
	}

	public Optional<String> getReason() {
		return Optional.ofNullable(reason);
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public boolean allows(MfaFactorType factorType) {
		Objects.requireNonNull(factorType, "factorType");
		return acceptableFactors.contains(factorType);
	}
}
