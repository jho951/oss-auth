package com.auth.serviceaccount;

import com.auth.core.api.model.Principal;
import java.security.cert.X509Certificate;
import java.util.Optional;

/** 인증된 X.509 서비스 식별자를 공통 principal 모델로 변환합니다. */
public interface X509ServicePrincipalResolver {

	Optional<Principal> resolve(X509Certificate certificate);
}
