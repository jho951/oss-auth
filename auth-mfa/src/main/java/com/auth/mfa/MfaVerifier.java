package com.auth.mfa;

import java.util.Map;
import java.util.Optional;

/** 등록된 2차 인증 수단 하나를 검증하고 추가 검증 속성을 반환할 수 있습니다. */
public interface MfaVerifier {

	boolean supports(MfaFactorType factorType);

	Optional<Map<String, Object>> verify(MfaVerificationRequest request, MfaEnrollment enrollment);
}
