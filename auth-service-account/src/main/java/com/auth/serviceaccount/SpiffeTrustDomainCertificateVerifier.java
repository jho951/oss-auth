package com.auth.serviceaccount;

import com.auth.core.utils.Strings;
import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.Set;

/** 허용된 trust domain의 SPIFFE ID를 가진 workload 인증서만 허용합니다. */
public final class SpiffeTrustDomainCertificateVerifier implements X509ServiceCertificateVerifier {

	private final Set<String> allowedTrustDomains;
	private final String requiredPathPrefix;

	public SpiffeTrustDomainCertificateVerifier(Set<String> allowedTrustDomains, String requiredPathPrefix) {
		this.allowedTrustDomains = allowedTrustDomains == null ? com.auth.core.utils.CollectionUtils.setOf() : com.auth.core.utils.CollectionUtils.copySet(allowedTrustDomains);
		this.requiredPathPrefix = requiredPathPrefix == null ? "" : requiredPathPrefix;
	}

	@Override
	public boolean verify(X509Certificate certificate) {
		return X509SubjectAlternativeNameUtils.uriSubjectAlternativeNames(certificate).stream()
			.map(uri -> {
				try {
					return SpiffeId.parse(uri.toString());
				} catch (RuntimeException ignored) {
					return null;
				}
			})
			.filter(Objects::nonNull)
			.anyMatch(this::allowed);
	}

	private boolean allowed(SpiffeId spiffeId) {
		boolean trustDomainAllowed = allowedTrustDomains.isEmpty() || allowedTrustDomains.contains(spiffeId.getTrustDomain());
		boolean pathAllowed = Strings.isBlank(requiredPathPrefix) || spiffeId.getWorkloadPath().startsWith(requiredPathPrefix);
		return trustDomainAllowed && pathAllowed;
	}
}
