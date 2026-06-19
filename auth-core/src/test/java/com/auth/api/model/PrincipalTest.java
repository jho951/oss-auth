package com.auth.api.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

import com.auth.core.api.model.Principal;

class PrincipalTest {

	@Test
	@DisplayName("사용자 ID와 부가 속성 맵으로 Principal 객체를 생성한다.")
	void createPrincipal_Success() {
		Map<String, Object> attrs = com.auth.core.utils.CollectionUtils.mapOf("email", "user@example.com", "dept", "IT");
		Principal principal = new Principal("user-1", attrs);

		assertThat(principal.getUserId()).isEqualTo("user-1");
		assertThat(principal.getAttributes())
			.hasSize(2)
			.containsEntry("email", "user@example.com")
			.containsEntry("dept", "IT");
	}

	@Test
	@DisplayName("attributes가 null이면 빈 맵으로 초기화된다.")
	void createPrincipal_NullAttributes_EmptyMap() {
		Principal principal = new Principal("user-1");
		assertThat(principal.getAttributes()).isEmpty();
	}

	@Test
	@DisplayName("getAttribute는 특정 키에 해당하는 값을 정확히 반환한다.")
	void getAttribute_Check() {
		Principal principal = new Principal("user-1", com.auth.core.utils.CollectionUtils.mapOf("type", "INTERNAL"));

		assertThat(principal.getAttribute("type")).isEqualTo("INTERNAL");
		assertThat(principal.getAttribute("non-existent")).isNull();
	}

	@Test
	@DisplayName("반환된 속성 맵을 외부에서 수정하려고 하면 예외가 발생한다 (불변성).")
	void getAttributes_IsUnmodifiable() {
		Map<String, Object> mutableMap = new HashMap<>();
		mutableMap.put("key", "value");

		Principal principal = new Principal("user-1", mutableMap);
		Map<String, Object> attributes = principal.getAttributes();

		// 불변성 테스트: 외부에서 수정을 시도할 때 예외 발생 확인
		assertThatThrownBy(() -> attributes.put("new-key", "new-value"))
			.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	void createPrincipal_WithAuthoritiesContainsAuthority() {
		Principal principal = new Principal("user-42", com.auth.core.utils.CollectionUtils.listOf("USER", "ADMIN"));

		assertThat(principal.getAuthorities()).containsExactly("USER", "ADMIN");
		assertThat(principal.getAuthorities()).contains("ADMIN");
		assertThat(principal.getAuthorities()).doesNotContain("GUEST");
	}
}
