package com.auth.api.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.auth.common.utils.Strings;

/**
 * OAuth2/OIDC provider 인증이 끝난 뒤 내부 사용자 매핑에 사용하는 표준 모델입니다.
 */
public final class OAuth2UserIdentity {

	private final String provider;
	private final String providerUserId;
	private final String email;
	private final String name;
	private final Map<String, Object> attributes;

	public OAuth2UserIdentity(
		String provider,
		String providerUserId,
		String email,
		String name,
		Map<String, Object> attributes
	) {
		this.provider = Strings.requireNonBlank(provider, "provider");
		this.providerUserId = Strings.requireNonBlank(providerUserId, "providerUserId");
		this.email = email;
		this.name = name;
		this.attributes = attributes == null
			? Collections.emptyMap()
			: Collections.unmodifiableMap(new LinkedHashMap<>(attributes));
	}

	public String getProvider() {
		return provider;
	}

	public String getProviderUserId() {
		return providerUserId;
	}

	public String getEmail() {
		return email;
	}

	public String getName() {
		return name;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}
}
