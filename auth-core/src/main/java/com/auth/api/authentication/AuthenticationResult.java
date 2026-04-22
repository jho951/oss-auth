package com.auth.api.authentication;

import com.auth.api.model.Principal;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** 인증이 성공하면 만들어지는 인증 확인서 */
public final class AuthenticationResult {

	/** 사용자 ID 등 핵심 정보 (주체) */
	private final Principal principal;
	/** JWT, Session, API Key 등 (수단) */
	private final AuthenticationSource source;
	/** 토큰 만료나 세션 연장 (기간) */
	private final Instant authenticatedAt;
	/** 접속 IP, 브라우저 정보, 특정 권한 (추가 정보) */
	private final Map<String, Object> attributes;

	public AuthenticationResult(Principal principal, AuthenticationSource source) {
		this(principal, source, Instant.now(), Map.of());
	}

	public AuthenticationResult(Principal principal, AuthenticationSource source, Instant authenticatedAt, Map<String, Object> attributes) {
		this.principal = Objects.requireNonNull(principal, "principal");
		this.source = Objects.requireNonNull(source, "source");
		this.authenticatedAt = authenticatedAt == null ? Instant.now() : authenticatedAt;
		this.attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
	}

	public Principal getPrincipal() {
		return principal;
	}
	public AuthenticationSource getSource() {
		return source;
	}
	public Instant getAuthenticatedAt() {
		return authenticatedAt;
	}
	public Map<String, Object> getAttributes() {
		return attributes;
	}
	public Optional<Object> getAttribute(String key) {
		return Optional.ofNullable(attributes.get(key));
	}
}
