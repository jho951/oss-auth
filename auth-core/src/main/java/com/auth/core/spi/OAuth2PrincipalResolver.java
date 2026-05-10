package com.auth.core.spi;

import com.auth.core.api.model.OAuth2UserIdentity;
import com.auth.core.api.model.Principal;

/** 외부 OAuth2/OIDC provider 사용자 정보를 내부 `Principal` 로 변환 */
public interface OAuth2PrincipalResolver {

	Principal resolve(OAuth2UserIdentity identity);
}
