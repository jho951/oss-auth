package com.auth.mtls;

import java.security.cert.X509Certificate;

/** principal 매핑 전에 제시된 클라이언트 인증서를 검증합니다. */
public interface MtlsCertificateVerifier {

	boolean verify(X509Certificate certificate);
}
