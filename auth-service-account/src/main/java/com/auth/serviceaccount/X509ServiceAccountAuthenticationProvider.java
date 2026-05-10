package com.auth.serviceaccount;

import com.auth.core.api.authentication.AuthenticationProvider;
import com.auth.core.api.model.Principal;
import java.util.Objects;
import java.util.Optional;

/** X.509 workload 인증서를 서비스 principal로 인증합니다. */
public final class X509ServiceAccountAuthenticationProvider implements AuthenticationProvider<X509ServiceCredential> {

	private final X509ServiceCertificateVerifier certificateVerifier;
	private final X509ServicePrincipalResolver principalResolver;

	public X509ServiceAccountAuthenticationProvider(
		X509ServiceCertificateVerifier certificateVerifier,
		X509ServicePrincipalResolver principalResolver
	) {
		this.certificateVerifier = Objects.requireNonNull(certificateVerifier, "certificateVerifier");
		this.principalResolver = Objects.requireNonNull(principalResolver, "principalResolver");
	}

	@Override
	public Optional<Principal> authenticate(X509ServiceCredential credential) {
		if (credential == null || credential.certificate() == null) return Optional.empty();
		if (!certificateVerifier.verify(credential.certificate())) return Optional.empty();
		return principalResolver.resolve(credential.certificate());
	}
}
