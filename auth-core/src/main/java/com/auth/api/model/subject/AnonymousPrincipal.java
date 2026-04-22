package com.auth.api.model.subject;

import com.auth.api.model.AuthoritySet;
import com.auth.api.model.PrincipalType;
import java.util.Map;

/** 익명 사용자 처리 (로그인하지 않은 사용자와 일반 회원을 똑같은 데이터 형식으로 다루기 위해) */
public final class AnonymousPrincipal {

	private AnonymousPrincipal() {}

	public static AuthenticatedSubject create() {
		return new AuthenticatedSubject("anonymous", PrincipalType.ANONYMOUS, AuthoritySet.empty(), Map.of());
	}
}
