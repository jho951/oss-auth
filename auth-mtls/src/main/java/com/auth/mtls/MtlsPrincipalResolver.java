package com.auth.mtls;

import com.auth.core.api.model.Principal;
import java.security.cert.X509Certificate;
import java.util.Optional;

/** 인증된 클라이언트 인증서를 공통 principal 모델로 변환합니다. */
public interface MtlsPrincipalResolver {

	Optional<Principal> resolve(X509Certificate certificate);
}
