package com.auth.saml;

/** 서명과 XML 처리를 포함한 원시 SAML 응답을 검증해 정규화된 assertion으로 변환합니다. */
public interface SamlResponseVerifier {

	SamlAssertion verify(SamlAuthenticationRequest request);
}
