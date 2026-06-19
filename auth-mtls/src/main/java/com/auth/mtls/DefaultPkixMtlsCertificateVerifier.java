package com.auth.mtls;

import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertificateFactory;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** trust anchor와 선택적 체인 조회기를 이용해 PKIX 검증을 수행하는 기본 mTLS verifier입니다. */
public final class DefaultPkixMtlsCertificateVerifier implements MtlsCertificateVerifier {

	private final Set<TrustAnchor> trustAnchors;
	private final MtlsCertificateChainResolver chainResolver;
	private final Clock clock;

	public DefaultPkixMtlsCertificateVerifier(Set<X509Certificate> trustAnchors) {
		this(trustAnchors, certificate -> com.auth.core.utils.CollectionUtils.listOf(), Clock.systemUTC());
	}

	public DefaultPkixMtlsCertificateVerifier(
		Set<X509Certificate> trustAnchors,
		MtlsCertificateChainResolver chainResolver,
		Clock clock
	) {
		this.trustAnchors = toTrustAnchors(trustAnchors);
		this.chainResolver = chainResolver == null ? certificate -> com.auth.core.utils.CollectionUtils.listOf() : chainResolver;
		this.clock = Objects.requireNonNull(clock, "clock");
	}

	@Override
	public boolean verify(X509Certificate certificate) {
		if (certificate == null || trustAnchors.isEmpty()) return false;
		try {
			certificate.checkValidity(now());
			if (isTrustedLeaf(certificate)) {
				return true;
			}

			List<X509Certificate> pathCertificates = new ArrayList<>();
			pathCertificates.add(certificate);
			List<X509Certificate> additionalCertificates = chainResolver.resolve(certificate);
			if (additionalCertificates != null) {
				for (X509Certificate additionalCertificate : additionalCertificates) {
					if (additionalCertificate != null && !sameCertificate(certificate, additionalCertificate)) {
						pathCertificates.add(additionalCertificate);
					}
				}
			}

			CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
			CertPath certPath = certificateFactory.generateCertPath(pathCertificates);
			java.security.cert.PKIXParameters parameters = new java.security.cert.PKIXParameters(trustAnchors);
			parameters.setRevocationEnabled(false);
			parameters.setDate(now());
			X509CertSelector selector = new X509CertSelector();
			selector.setCertificate(certificate);
			parameters.setTargetCertConstraints(selector);

			CertPathValidator.getInstance("PKIX").validate(certPath, parameters);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private boolean isTrustedLeaf(X509Certificate certificate) {
		return trustAnchors.stream()
			.map(TrustAnchor::getTrustedCert)
			.filter(Objects::nonNull)
			.anyMatch(anchor -> sameCertificate(anchor, certificate));
	}

	private Date now() {
		return Date.from(Instant.now(clock));
	}

	private static Set<TrustAnchor> toTrustAnchors(Set<X509Certificate> trustAnchors) {
		if (trustAnchors == null || trustAnchors.isEmpty()) {
			return com.auth.core.utils.CollectionUtils.setOf();
		}
		LinkedHashSet<TrustAnchor> anchors = new LinkedHashSet<>();
		for (X509Certificate trustAnchor : trustAnchors) {
			if (trustAnchor != null) {
				anchors.add(new TrustAnchor(trustAnchor, null));
			}
		}
		return com.auth.core.utils.CollectionUtils.copySet(anchors);
	}

	private static boolean sameCertificate(X509Certificate left, X509Certificate right) {
		try {
			return Arrays.equals(left.getEncoded(), right.getEncoded());
		} catch (Exception e) {
			return left.equals(right);
		}
	}
}
