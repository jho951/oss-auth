package com.auth.serviceaccount;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import org.junit.jupiter.api.Test;

class X509ServiceAccountAuthenticationProviderTest {

	@Test
	void authenticatesSpiffeWorkloadCertificate() {
		X509Certificate certificate = new StubCertificate("workload", com.auth.core.utils.CollectionUtils.listOf(com.auth.core.utils.CollectionUtils.listOf(6, "spiffe://example.org/ns/payments/sa/billing")));
		X509ServiceAccountAuthenticationProvider provider = new X509ServiceAccountAuthenticationProvider(
			new SpiffeTrustDomainCertificateVerifier(com.auth.core.utils.CollectionUtils.setOf("example.org"), "/ns/payments"),
			new SpiffePrincipalResolver()
		);

		Optional<com.auth.core.api.model.Principal> result = provider.authenticate(new X509ServiceCredential(certificate));

		assertThat(result).isPresent();
		assertThat(result.get().getUserId()).isEqualTo("spiffe://example.org/ns/payments/sa/billing");
		assertThat(result.get().getAttribute("trust_domain")).isEqualTo("example.org");
	}

	@Test
	void parsesSpiffeId() {
		SpiffeId spiffeId = SpiffeId.parse("spiffe://example.org/ns/payments/sa/billing");

		assertThat(spiffeId.getTrustDomain()).isEqualTo("example.org");
		assertThat(spiffeId.getWorkloadPath()).isEqualTo("/ns/payments/sa/billing");
	}

	private static final class StubCertificate extends X509Certificate {
		private final X500Principal principal;
		private final Collection<List<?>> sans;

		private StubCertificate(String commonName, Collection<List<?>> sans) {
			this.principal = new X500Principal("CN=" + commonName);
			this.sans = sans;
		}

		@Override public Collection<List<?>> getSubjectAlternativeNames() {return sans;}
		@Override public byte[] getEncoded() throws CertificateEncodingException {return new byte[] {1, 2, 3};}
		@Override public void verify(PublicKey key) {}
		@Override public void verify(PublicKey key, String sigProvider) {}
		@Override public String toString() {return principal.getName();}
		@Override public PublicKey getPublicKey() {return null;}
		@Override public void checkValidity() {}
		@Override public void checkValidity(Date date) {}
		@Override public int getVersion() {return 3;}
		@Override public BigInteger getSerialNumber() {return BigInteger.ONE;}
		@Override public java.security.Principal getIssuerDN() {return principal;}
		@Override public java.security.Principal getSubjectDN() {return principal;}
		@Override public Date getNotBefore() {return new Date();}
		@Override public Date getNotAfter() {return new Date(System.currentTimeMillis() + 1_000L);}
		@Override public byte[] getTBSCertificate() {return new byte[] {1};}
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
