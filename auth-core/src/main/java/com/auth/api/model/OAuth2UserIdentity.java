package com.auth.api.model;

import com.auth.common.utils.Strings;

import java.util.Map;
import java.util.Collections;
import java.util.LinkedHashMap;

/** OAuth2/OIDC provider 인증이 끝난 뒤 데이터를 지정 양식으로 정리한 신분증 */
public final class OAuth2UserIdentity {

	/** 제공 주체 (예: GOOGLE, GitHub, KaKao 등) */
	private final String provider;
	/** 제공 주체 (예: GOOGLE, GitHub, KaKao 등) */
	private final String providerUserId;
	/** 사용자 이메일 */
	private final String email;
	/** 사용자 이름 */
	private final String name;
	/** 추가로 넘어온 모든 데이터(프로필 이미지, 성별 등) */
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
