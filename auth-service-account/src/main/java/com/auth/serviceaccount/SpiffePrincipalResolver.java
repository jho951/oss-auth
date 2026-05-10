package com.auth.serviceaccount;

import com.auth.core.api.model.Principal;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Optional;

/** 인증서의 첫 번째 SPIFFE URI SAN을 머신 principal로 변환합니다. */
public final class SpiffePrincipalResolver implements X509ServicePrincipalResolver {

	@Override
	public Optional<Principal> resolve(X509Certificate certificate) {
		return X509SubjectAlternativeNameUtils.uriSubjectAlternativeNames(certificate).stream()
			.map(uri -> {
				try {
					return SpiffeId.parse(uri.toString());
				} catch (RuntimeException ignored) {
					return null;
				}
			})
			.filter(spiffeId -> spiffeId != null)
			.findFirst()
			.map(spiffeId -> new Principal(
				spiffeId.toString(),
				Map.of(
					"credential_type", "x509_service",
					"trust_domain", spiffeId.getTrustDomain(),
					"workload_path", spiffeId.getWorkloadPath()
				)
			));
	}
}
