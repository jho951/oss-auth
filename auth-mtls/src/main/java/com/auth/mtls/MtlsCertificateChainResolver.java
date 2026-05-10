package com.auth.mtls;

import java.security.cert.X509Certificate;
import java.util.List;

/** 리프 인증서 검증에 필요한 추가 인증서 체인을 조회합니다. */
public interface MtlsCertificateChainResolver {

	List<X509Certificate> resolve(X509Certificate certificate);
}
