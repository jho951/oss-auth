package com.auth.saml;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class DefaultSamlResponseVerifierTest {

	@Test
	void verifiesSignatureAndExtractsNormalizedAssertion() {
		AtomicInteger invocations = new AtomicInteger();
		DefaultSamlResponseVerifier verifier = new DefaultSamlResponseVerifier(
			responseXml -> invocations.incrementAndGet(),
			new DomSamlAssertionExtractor()
		);

		SamlAssertion assertion = verifier.verify(new SamlAuthenticationRequest(sampleResponseXml(), "", "", "", com.auth.core.utils.CollectionUtils.mapOf()));

		assertThat(invocations.get()).isEqualTo(1);
		assertThat(assertion.getSubject()).isEqualTo("user-1");
		assertThat(assertion.getIssuer()).isEqualTo("https://idp.example.com");
		assertThat(assertion.getAudiences()).containsExactly("urn:test:sp");
		assertThat(assertion.getRecipient()).isEqualTo("https://sp.example.com/acs");
		assertThat(assertion.getInResponseTo()).isEqualTo("req-1");
		assertThat(assertion.getSessionIndex()).isEqualTo("session-1");
		assertThat(assertion.getAttributes().get("email")).isEqualTo("user@example.com");
		assertThat(assertion.getAttributes().get("groups")).isEqualTo(com.auth.core.utils.CollectionUtils.listOf("admin", "ops"));
	}

	private static String sampleResponseXml() {
		return String.join("\n",
			"<samlp:Response xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\"",
			"                xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\">",
			"  <saml:Assertion IssueInstant=\"2026-01-01T00:00:00Z\">",
			"    <saml:Issuer>https://idp.example.com</saml:Issuer>",
			"    <saml:Subject>",
			"      <saml:NameID>user-1</saml:NameID>",
			"      <saml:SubjectConfirmation>",
			"        <saml:SubjectConfirmationData Recipient=\"https://sp.example.com/acs\" InResponseTo=\"req-1\" />",
			"      </saml:SubjectConfirmation>",
			"    </saml:Subject>",
			"    <saml:Conditions NotBefore=\"2025-12-31T23:59:00Z\" NotOnOrAfter=\"2026-01-01T01:00:00Z\">",
			"      <saml:AudienceRestriction>",
			"        <saml:Audience>urn:test:sp</saml:Audience>",
			"      </saml:AudienceRestriction>",
			"    </saml:Conditions>",
			"    <saml:AttributeStatement>",
			"      <saml:Attribute Name=\"email\">",
			"        <saml:AttributeValue>user@example.com</saml:AttributeValue>",
			"      </saml:Attribute>",
			"      <saml:Attribute Name=\"groups\">",
			"        <saml:AttributeValue>admin</saml:AttributeValue>",
			"        <saml:AttributeValue>ops</saml:AttributeValue>",
			"      </saml:Attribute>",
			"    </saml:AttributeStatement>",
			"    <saml:AuthnStatement SessionIndex=\"session-1\" />",
			"  </saml:Assertion>",
			"</samlp:Response>"
		);
	}
}
