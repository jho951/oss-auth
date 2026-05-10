package com.auth.mfa;

import com.auth.core.api.model.Principal;
import java.util.List;

/** 현재 액션이나 위험도에 따라 MFA step-up이 필요한지 판단합니다. */
public interface MfaPolicy {

	MfaRequirement evaluate(Principal principal, MfaChallengeContext context, List<MfaEnrollment> enrollments);
}
