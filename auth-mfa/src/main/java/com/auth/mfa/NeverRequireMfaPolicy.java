package com.auth.mfa;

import com.auth.core.api.model.Principal;
import java.util.List;

/** MFA를 요구하지 않는 정책입니다. */
public final class NeverRequireMfaPolicy implements MfaPolicy {

	@Override
	public MfaRequirement evaluate(Principal principal, MfaChallengeContext context, List<MfaEnrollment> enrollments) {
		return MfaRequirement.notRequired();
	}
}
