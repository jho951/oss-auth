package com.auth.api.model.subject;

import com.auth.api.model.AuthoritySet;
import com.auth.api.model.PrincipalType;
import java.util.Map;

/** 직접 아이디와 비밀번호를 입력해서 로그인하는 일반적인 유저 생성 도구 */
public final class UserPrincipal {

	private UserPrincipal() {}

	public static AuthenticatedSubject of(String userId, AuthoritySet authorities, Map<String, Object> attributes) {
		return new AuthenticatedSubject(userId, PrincipalType.USER, authorities, attributes);
	}
}
