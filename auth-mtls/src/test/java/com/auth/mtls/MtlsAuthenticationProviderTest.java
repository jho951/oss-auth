package com.auth.mtls;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.X509Certificate;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import org.junit.jupiter.api.Test;

class MtlsAuthenticationProviderTest {

	@Test
	void authenticatesVerifiedCertificate() {
		X509Certificate certificate = new StubCertificate("device-1", new byte[] {1, 2, 3});
		MtlsAuthenticationProvider provider = new MtlsAuthenticationProvider(
			cert -> true,
			cert -> Optional.of(new com.auth.core.api.model.Principal(cert.getSubjectX500Principal().getName()))
		);

		var result = provider.authenticate(new MtlsClientCertificateCredential(certificate));

		assertThat(result).isPresent();
		assertThat(result.orElseThrow().getUserId()).contains("CN=device-1");
	}

	@Test
	void matchesCertificateBoundConfirmationClaim() {
		X509Certificate certificate = new StubCertificate("device-1", new byte[] {9, 8, 7});
		String thumbprint = X509ThumbprintUtils.sha256Thumbprint(certificate);

		assertThat(X509ThumbprintUtils.matchesConfirmation(
			Map.of("cnf", Map.of("x5t#S256", thumbprint)),
			certificate
		)).isTrue();
	}

	@Test
	void verifiesTrustedLeafCertificateWithPkixVerifier() {
		X509Certificate certificate = new StubCertificate("device-1", new byte[] {4, 5, 6});
		DefaultPkixMtlsCertificateVerifier verifier = new DefaultPkixMtlsCertificateVerifier(Set.of(certificate));

		assertThat(verifier.verify(certificate)).isTrue();
	}

	@Test
	void rejectsExpiredTrustedLeafCertificateWithPkixVerifier() {
		X509Certificate certificate = new StubCertificate("device-1", new byte[] {7, 8, 9}, true);
		DefaultPkixMtlsCertificateVerifier verifier = new DefaultPkixMtlsCertificateVerifier(
			Set.of(certificate),
			leaf -> List.of(),
			Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC)
		);

		assertThat(verifier.verify(certificate)).isFalse();
	}

	private static final class StubCertificate extends X509Certificate {
		private final X500Principal principal;
		private final byte[] encoded;
		private final boolean expired;

		private StubCertificate(String commonName, byte[] encoded) {
			this(commonName, encoded, false);
		}

		private StubCertificate(String commonName, byte[] encoded, boolean expired) {
			this.principal = new X500Principal("CN=" + commonName);
			this.encoded = encoded.clone();
			this.expired = expired;
		}

		@Override
		public byte[] getEncoded() throws CertificateEncodingException {
			return encoded.clone();
		}

		@Override public void verify(PublicKey key) {}
		@Override public void verify(PublicKey key, String sigProvider) {}
		@Override public String toString() {return principal.getName();}
		@Override public PublicKey getPublicKey() {return null;}
		@Override
		public void checkValidity() throws CertificateExpiredException {
			if (expired) throw new CertificateExpiredException("expired");
		}

		@Override
		public void checkValidity(Date date) throws CertificateExpiredException {
			if (expired) throw new CertificateExpiredException("expired");
		}
		@Override public int getVersion() {return 3;}
		@Override public BigInteger getSerialNumber() {return BigInteger.ONE;}
		@Override public java.security.Principal getIssuerDN() {return principal;}
		@Override public java.security.Principal getSubjectDN() {return principal;}
		@Override public Date getNotBefore() {return new Date();}
		@Override public Date getNotAfter() {return new Date(System.currentTimeMillis() + 1_000L);}
		@Override public byte[] getTBSCertificate() {return encoded.clone();}
		@Override public byte[] getSignature() {return new byte[0];}
		@Override public String getSigAlgName() {return "NONE";}
		@Override public String getSigAlgOID() {return "0.0";}
		@Override public byte[] getSigAlgParams() {return new byte[0];}
		@Override public boolean[] getIssuerUniqueID() {return null;}
		@Override public boolean[] getSubjectUniqueID() {return null;}
		@Override public boolean[] getKeyUsage() {return null;}
		@Override public int getBasicConstraints() {return -1;}
		@Override public X500Principal getSubjectX500Principal() {return principal;}
		@Override public X500Principal getIssuerX500Principal() {return principal;}
		@Override public Set<String> getCriticalExtensionOIDs() {return null;}
		@Override public byte[] getExtensionValue(String oid) {return null;}
		@Override public Set<String> getNonCriticalExtensionOIDs() {return null;}
		@Override public boolean hasUnsupportedCriticalExtension() {return false;}
	}
}
