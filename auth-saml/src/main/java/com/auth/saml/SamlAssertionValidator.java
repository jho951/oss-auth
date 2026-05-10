package com.auth.saml;

/** 저수준 서명/XML 검증이 끝난 뒤 정규화된 SAML 조건을 검증합니다. */
public interface SamlAssertionValidator {

	void validate(SamlAssertion assertion, SamlAuthenticationRequest request);
}
