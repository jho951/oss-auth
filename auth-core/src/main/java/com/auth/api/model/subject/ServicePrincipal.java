package com.auth.api.model.subject;

import com.auth.api.model.AuthoritySet;
import com.auth.api.model.PrincipalType;
import java.util.Map;

/** 사람이 아닌 '프로그램'이나 '서버'에게 발급하는 전용 신분증을 만드는 생성 도구 */
public final class ServicePrincipal {

	private ServicePrincipal() {}

	public static AuthenticatedSubject of(String serviceId, AuthoritySet authorities, Map<String, Object> attributes) {
		return new AuthenticatedSubject(serviceId, PrincipalType.SERVICE, authorities, attributes);
	}
}
