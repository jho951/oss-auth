package com.auth.saml;

/** 검증된 SAML 응답 XML에서 정규화된 assertion을 추출합니다. */
public interface SamlAssertionExtractor {

	SamlAssertion extract(String responseXml);
}
