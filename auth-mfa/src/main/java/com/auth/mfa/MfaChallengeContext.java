package com.auth.mfa;

import java.util.Map;
import java.util.Optional;

/** step-up 판단이 필요한 이유를 설명하는 컨텍스트입니다. */
public final class MfaChallengeContext {

	private static final MfaChallengeContext EMPTY = new MfaChallengeContext(null, MfaRiskLevel.UNKNOWN, Map.of());

	private final String action;
	private final MfaRiskLevel riskLevel;
	private final Map<String, Object> attributes;

	public MfaChallengeContext(String action, MfaRiskLevel riskLevel, Map<String, Object> attributes) {
		this.action = action == null ? "" : action.trim();
		this.riskLevel = riskLevel == null ? MfaRiskLevel.UNKNOWN : riskLevel;
		this.attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
	}

	public static MfaChallengeContext empty() {
		return EMPTY;
	}

	public String getAction() {
		return action;
	}

	public Optional<String> getActionIfPresent() {
		return action.isBlank() ? Optional.empty() : Optional.of(action);
	}

	public MfaRiskLevel getRiskLevel() {
		return riskLevel;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}
}
