package com.auth.serviceaccount;

import java.security.cert.X509Certificate;

/** 워크로드나 머신 principal이 제시하는 X.509 서비스 자격 증명입니다. */
public final class X509ServiceCredential {

	private final X509Certificate certificate;

	public X509ServiceCredential(X509Certificate certificate) {
		this.certificate = certificate;
	}

	public X509Certificate certificate() {
		return certificate;
	}
}
