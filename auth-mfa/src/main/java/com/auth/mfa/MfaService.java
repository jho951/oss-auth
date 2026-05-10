package com.auth.mfa;

import com.auth.core.api.model.Principal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** step-up 필요 여부 판단과 2차 인증 검증을 조정하는 서비스입니다. */
public final class MfaService {

	private final MfaEnrollmentResolver enrollmentResolver;
	private final MfaPolicy policy;
	private final List<MfaVerifier> verifiers;
	private final MfaPrincipalMapper principalMapper;

	public MfaService(
		MfaEnrollmentResolver enrollmentResolver,
		MfaPolicy policy,
		List<MfaVerifier> verifiers,
		MfaPrincipalMapper principalMapper
	) {
		this.enrollmentResolver = Objects.requireNonNull(enrollmentResolver, "enrollmentResolver");
		this.policy = Objects.requireNonNull(policy, "policy");
		this.verifiers = verifiers == null ? List.of() : List.copyOf(verifiers);
		this.principalMapper = principalMapper == null ? new DefaultMfaPrincipalMapper() : principalMapper;
	}

	public MfaRequirement evaluate(Principal principal, MfaChallengeContext context) {
		Objects.requireNonNull(principal, "principal");
		return policy.evaluate(principal, context == null ? MfaChallengeContext.empty() : context, resolveEnrollments(principal));
	}

	public Optional<Principal> stepUp(MfaVerificationRequest request) {
		if (request == null) return Optional.empty();
		List<MfaEnrollment> enrollments = resolveEnrollments(request.getPrincipal());
		MfaRequirement requirement = policy.evaluate(request.getPrincipal(), request.getContext(), enrollments);
		if (requirement.isRequired() && !requirement.allows(request.getFactorType())) {
			return Optional.empty();
		}

		Optional<MfaEnrollment> enrollment = findEnrollment(enrollments, request);
		if (enrollment.isEmpty()) return Optional.empty();

		Optional<MfaVerifier> verifier = findVerifier(request.getFactorType());
		if (verifier.isEmpty()) return Optional.empty();

		return verifier.get()
			.verify(request, enrollment.get())
			.map(attributes -> principalMapper.map(request.getPrincipal(), enrollment.get(), attributes, request.getContext()));
	}

	private List<MfaEnrollment> resolveEnrollments(Principal principal) {
		List<MfaEnrollment> enrollments = enrollmentResolver.resolve(principal);
		return enrollments == null ? List.of() : List.copyOf(enrollments);
	}

	private Optional<MfaEnrollment> findEnrollment(List<MfaEnrollment> enrollments, MfaVerificationRequest request) {
		return request.getFactorId()
			.map(factorId -> enrollments.stream()
				.filter(enrollment -> factorId.equals(enrollment.getFactorId()))
				.filter(enrollment -> enrollment.getFactorType() == request.getFactorType())
				.findFirst())
			.orElseGet(() -> uniqueEnrollmentByType(enrollments, request.getFactorType()));
	}

	private Optional<MfaEnrollment> uniqueEnrollmentByType(List<MfaEnrollment> enrollments, MfaFactorType factorType) {
		MfaEnrollment match = null;
		for (MfaEnrollment enrollment : enrollments) {
			if (enrollment.getFactorType() != factorType) continue;
			if (match != null) return Optional.empty();
			match = enrollment;
		}
		return Optional.ofNullable(match);
	}

	private Optional<MfaVerifier> findVerifier(MfaFactorType factorType) {
		return verifiers.stream().filter(verifier -> verifier.supports(factorType)).findFirst();
	}
}
