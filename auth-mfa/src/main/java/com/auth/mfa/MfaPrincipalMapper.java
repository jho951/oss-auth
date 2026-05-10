package com.auth.mfa;

import com.auth.core.api.model.Principal;
import java.util.Map;

/** step-up 성공 후 principal에 MFA 결과 메타데이터를 추가합니다. */
public interface MfaPrincipalMapper {

	Principal map(
		Principal principal,
		MfaEnrollment enrollment,
		Map<String, Object> verificationAttributes,
		MfaChallengeContext context
	);
}
