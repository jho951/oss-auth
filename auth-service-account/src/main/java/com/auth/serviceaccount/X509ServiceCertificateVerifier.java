package com.auth.serviceaccount;

import java.security.cert.X509Certificate;

/** principal 매핑 전에 X.509 서비스 인증서를 검증합니다. */
public interface X509ServiceCertificateVerifier {

	boolean verify(X509Certificate certificate);
}
