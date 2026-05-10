package com.auth.saml;

/** SAML 응답 XML의 서명과 신뢰 체인을 검증합니다. */
public interface SamlXmlSignatureVerifier {

	void verify(String responseXml);
}
