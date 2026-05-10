package com.auth.saml;

import com.auth.core.api.exception.AuthException;
import com.auth.core.api.exception.AuthFailureReason;
import com.auth.core.utils.Strings;
import java.util.Objects;

/** XML 서명 검증과 assertion 추출을 조합하는 기본 SAML 응답 verifier입니다. */
public final class DefaultSamlResponseVerifier implements SamlResponseVerifier {

	private final SamlXmlSignatureVerifier signatureVerifier;
	private final SamlAssertionExtractor assertionExtractor;

	public DefaultSamlResponseVerifier(SamlXmlSignatureVerifier signatureVerifier, SamlAssertionExtractor assertionExtractor) {
		this.signatureVerifier = Objects.requireNonNull(signatureVerifier, "signatureVerifier");
		this.assertionExtractor = Objects.requireNonNull(assertionExtractor, "assertionExtractor");
	}

	@Override
	public SamlAssertion verify(SamlAuthenticationRequest request) {
		if (request == null || Strings.isBlank(request.getResponseXml())) {
			throw new AuthException(AuthFailureReason.INVALID_TOKEN, "blank SAML response");
		}
		signatureVerifier.verify(request.getResponseXml());
		return assertionExtractor.extract(request.getResponseXml());
	}
}
