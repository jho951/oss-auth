package com.auth.saml;

import com.auth.core.api.model.Principal;

/** 검증된 SAML assertion을 공통 principal 모델로 변환합니다. */
public interface SamlPrincipalMapper {

	Principal map(SamlAssertion assertion);
}
