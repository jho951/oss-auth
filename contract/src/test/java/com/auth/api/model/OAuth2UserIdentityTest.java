package com.auth.api.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OAuth2UserIdentityTest {

	@Test
	@DisplayName("OAuth2 사용자 정보를 생성한다.")
	void createIdentity_Success() {
		OAuth2UserIdentity identity = new OAuth2UserIdentity(
			"google",
			"provider-user-1",
			"user@example.com",
			"Tester",
			Map.of("email_verified", true)
		);

		assertThat(identity.getProvider()).isEqualTo("google");
		assertThat(identity.getProviderUserId()).isEqualTo("provider-user-1");
		assertThat(identity.getEmail()).isEqualTo("user@example.com");
		assertThat(identity.getName()).isEqualTo("Tester");
		assertThat(identity.getAttributes()).containsEntry("email_verified", true);
	}

	@Test
	@DisplayName("attributes는 외부에서 수정할 수 없다.")
	void attributes_AreImmutable() {
		Map<String, Object> attributes = new HashMap<>();
		attributes.put("id", "123");

		OAuth2UserIdentity identity = new OAuth2UserIdentity("github", "123", null, null, attributes);

		attributes.put("id", "456");

		assertThat(identity.getAttributes()).containsEntry("id", "123");
		assertThatThrownBy(() -> identity.getAttributes().put("name", "blocked"))
			.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	@DisplayName("provider와 providerUserId는 비어 있을 수 없다.")
	void createIdentity_BlankRequiredFields_Fails() {
		assertThatThrownBy(() -> new OAuth2UserIdentity("", "id", null, null, null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("provider");
		assertThatThrownBy(() -> new OAuth2UserIdentity("google", " ", null, null, null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("providerUserId");
	}
}
