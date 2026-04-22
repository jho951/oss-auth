package com.auth.api.model.subject;

import com.auth.api.model.AuthoritySet;
import com.auth.api.model.Principal;
import com.auth.api.model.PrincipalType;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/** 인증이 완료된 사용자나 서비스의 최종 신분증*/
public final class AuthenticatedSubject implements Serializable {
	/** 자바에서 객체를 직렬화할 때 사용하는 객체의 버전 번호 */
	private static final long serialVersionUID = 1L;
	/** 사용자의 고유 식별자 (예: 이메일, UUID 등) */
	private final String id;
	/** 주체가 누구인지 구분 (예: 사람, 서비스 계정, 익명) */
	private final PrincipalType type;
	/** 주체(PrincipalType)가 할 수 있는 권한 목록 */
	private final AuthoritySet authorities;
	/** 부가 정보 (예: 이름, 프로필 사진 경로 등) */
	private final Map<String, Object> attributes;

	public AuthenticatedSubject(String id, PrincipalType type, AuthoritySet authorities, Map<String, Object> attributes) {
		this.id = Objects.requireNonNull(id, "id");
		this.type = type == null ? PrincipalType.UNKNOWN : type;
		this.authorities = authorities == null ? AuthoritySet.empty() : authorities;
		this.attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
	}

	public static AuthenticatedSubject fromPrincipal(Principal principal, PrincipalType type) {
		Objects.requireNonNull(principal, "principal");
		return new AuthenticatedSubject(
			principal.getUserId(),
			type,
			AuthoritySet.of(principal.getAuthorities()),
			principal.getAttributes()
		);
	}

	public Principal toPrincipal() {
		return new Principal(id, authorities.asList(), attributes);
	}
	public String getId() {
		return id;
	}
	public PrincipalType getType() {
		return type;
	}
	public AuthoritySet getAuthorities() {
		return authorities;
	}
	public Map<String, Object> getAttributes() {
		return attributes;
	}
}
