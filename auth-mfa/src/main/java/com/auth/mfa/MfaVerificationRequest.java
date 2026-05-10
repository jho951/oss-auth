package com.auth.mfa;

import com.auth.core.api.model.Principal;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** step-up 시도에 제출된 2차 인증 증명 요청입니다. */
public final class MfaVerificationRequest {

	private final Principal principal;
	private final String factorId;
	private final MfaFactorType factorType;
	private final MfaChallengeContext context;
	private final Map<String, Object> proof;

	public MfaVerificationRequest(
		Principal principal,
		String factorId,
		MfaFactorType factorType,
		MfaChallengeContext context,
		Map<String, Object> proof
	) {
		this.principal = Objects.requireNonNull(principal, "principal");
		this.factorId = factorId == null ? "" : factorId.trim();
		this.factorType = Objects.requireNonNull(factorType, "factorType");
		this.context = context == null ? MfaChallengeContext.empty() : context;
		this.proof = proof == null ? Map.of() : Map.copyOf(proof);
	}

	public Principal getPrincipal() {
		return principal;
	}

	public Optional<String> getFactorId() {
		return factorId.isBlank() ? Optional.empty() : Optional.of(factorId);
	}

	public MfaFactorType getFactorType() {
		return factorType;
	}

	public MfaChallengeContext getContext() {
		return context;
	}

	public Map<String, Object> getProof() {
		return proof;
	}

	public Optional<Object> getProof(String key) {
		return Optional.ofNullable(proof.get(key));
	}
}
