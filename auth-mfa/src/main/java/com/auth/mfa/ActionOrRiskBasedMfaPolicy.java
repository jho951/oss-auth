package com.auth.mfa;

import com.auth.core.api.model.Principal;
import com.auth.core.utils.Strings;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** 보호된 액션이거나 전달된 위험도가 임계값 이상일 때 MFA를 요구합니다. */
public final class ActionOrRiskBasedMfaPolicy implements MfaPolicy {

	private final Set<String> protectedActions;
	private final MfaRiskLevel minimumRiskLevel;
	private final List<MfaFactorType> preferredFactors;
	private final String reason;

	public ActionOrRiskBasedMfaPolicy(
		Collection<String> protectedActions,
		MfaRiskLevel minimumRiskLevel,
		Collection<MfaFactorType> preferredFactors,
		String reason
	) {
		this.protectedActions = normalizeActions(protectedActions);
		this.minimumRiskLevel = minimumRiskLevel;
		this.preferredFactors = preferredFactors == null ? com.auth.core.utils.CollectionUtils.listOf() : com.auth.core.utils.CollectionUtils.copyList(preferredFactors);
		this.reason = reason;
	}

	@Override
	public MfaRequirement evaluate(Principal principal, MfaChallengeContext context, List<MfaEnrollment> enrollments) {
		MfaChallengeContext challengeContext = context == null ? MfaChallengeContext.empty() : context;
		List<MfaEnrollment> resolvedEnrollments = enrollments == null ? com.auth.core.utils.CollectionUtils.listOf() : com.auth.core.utils.CollectionUtils.copyList(enrollments);

		boolean actionMatch = challengeContext.getActionIfPresent()
			.map(ActionOrRiskBasedMfaPolicy::normalizeAction)
			.map(protectedActions::contains)
			.orElse(false);
		boolean riskMatch = challengeContext.getRiskLevel().meetsOrExceeds(minimumRiskLevel);

		if (!actionMatch && !riskMatch) {
			return MfaRequirement.notRequired();
		}

		List<MfaFactorType> availableFactors = factorTypesFrom(resolvedEnrollments);
		List<MfaFactorType> acceptedFactors = preferredFactors.isEmpty()
			? availableFactors
			: preferredFactors.stream()
				.filter(availableFactors::contains)
				.collect(Collectors.toList());

		return MfaRequirement.required(
			acceptedFactors,
			resolveReason(actionMatch, riskMatch),
			com.auth.core.utils.CollectionUtils.mapOf(
				"action_match", actionMatch,
				"risk_match", riskMatch,
				"enrolled_factor_count", resolvedEnrollments.size()
			)
		);
	}

	private String resolveReason(boolean actionMatch, boolean riskMatch) {
		if (!Strings.isBlank(reason)) return reason;
		if (actionMatch && riskMatch) return "mfa required for protected action and elevated risk";
		if (actionMatch) return "mfa required for protected action";
		return "mfa required for elevated risk";
	}

	private static Set<String> normalizeActions(Collection<String> actions) {
		if (actions == null || actions.isEmpty()) return com.auth.core.utils.CollectionUtils.setOf();
		LinkedHashSet<String> normalized = new LinkedHashSet<>();
		for (String action : actions) {
			String value = normalizeAction(action);
			if (!Strings.isBlank(value)) normalized.add(value);
		}
		return com.auth.core.utils.CollectionUtils.copySet(normalized);
	}

	private static String normalizeAction(String action) {
		return action == null ? "" : action.trim().toLowerCase(Locale.ROOT);
	}

	private static List<MfaFactorType> factorTypesFrom(List<MfaEnrollment> enrollments) {
		ArrayList<MfaFactorType> factors = new ArrayList<>();
		for (MfaEnrollment enrollment : enrollments) {
			if (!factors.contains(enrollment.getFactorType())) {
				factors.add(enrollment.getFactorType());
			}
		}
		return factors;
	}
}
