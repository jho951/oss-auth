package com.auth.mtls;

import com.auth.core.api.authentication.AuthenticationProvider;
import com.auth.core.api.model.Principal;
import java.util.Objects;
import java.util.Optional;

/** 클라이언트 인증서 기반 요청을 처리하는 범용 mTLS 인증 provider입니다. */
public final class MtlsAuthenticationProvider implements AuthenticationProvider<MtlsClientCertificateCredential> {

	private final MtlsCertificateVerifier certificateVerifier;
	private final MtlsPrincipalResolver principalResolver;

	public MtlsAuthenticationProvider(MtlsCertificateVerifier certificateVerifier, MtlsPrincipalResolver principalResolver) {
		this.certificateVerifier = Objects.requireNonNull(certificateVerifier, "certificateVerifier");
		this.principalResolver = Objects.requireNonNull(principalResolver, "principalResolver");
	}

	@Override
	public Optional<Principal> authenticate(MtlsClientCertificateCredential credential) {
		if (credential == null || credential.certificate() == null) return Optional.empty();
		if (!certificateVerifier.verify(credential.certificate())) return Optional.empty();
		return principalResolver.resolve(credential.certificate());
	}
}
