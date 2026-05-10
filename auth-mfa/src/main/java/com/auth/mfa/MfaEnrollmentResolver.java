package com.auth.mfa;

import com.auth.core.api.model.Principal;
import java.util.List;

/** 이미 인증된 principal에 연결된 2차 인증 수단을 조회합니다. */
public interface MfaEnrollmentResolver {

	List<MfaEnrollment> resolve(Principal principal);
}
