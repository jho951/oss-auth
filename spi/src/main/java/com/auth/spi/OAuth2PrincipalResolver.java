package com.auth.spi;

import com.auth.api.model.OAuth2UserIdentity;
import com.auth.api.model.Principal;

/** OAuth2/OIDC provider 사용자 정보를 내부 Principal로 매핑하는 포트입니다. */
public interface OAuth2PrincipalResolver {

	/**
	 * Provider 인증이 끝난 사용자 정보를 내부 사용자로 연결하고 Principal을 반환합니다.
	 */
	Principal resolve(OAuth2UserIdentity identity);
}