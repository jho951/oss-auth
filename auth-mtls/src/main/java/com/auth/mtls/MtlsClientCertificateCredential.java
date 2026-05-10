package com.auth.mtls;

import java.security.cert.X509Certificate;

/** mTLS 연결에서 추출한 정규화 클라이언트 인증서 자격 증명입니다. */
public record MtlsClientCertificateCredential(X509Certificate certificate) {
}
