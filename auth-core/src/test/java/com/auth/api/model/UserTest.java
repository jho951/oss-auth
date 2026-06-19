package com.auth.api.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

import com.auth.core.api.model.User;

class UserTest {

	@Test
	@DisplayName("User 객체 생성 시 필드 값이 올바르게 할당된다.")
	void createUser_Success() {
		User user = new User("uuid-1", "tester", "hashed-pw", com.auth.core.utils.CollectionUtils.listOf("USER"));

		assertThat(user.getUserId()).isEqualTo("uuid-1");
		assertThat(user.getUsername()).isEqualTo("tester");
		assertThat(user.getPasswordHash()).isEqualTo("hashed-pw");
		assertThat(user.getAuthorities()).containsExactly("USER");
	}

	@Test
	@DisplayName("필수 필드(userId, username, passwordHash)가 비어있으면 IllegalArgumentException이 발생한다.")
	void createUser_Fail_BlankFields() {
		assertThatThrownBy(() -> new User("", "tester", "hashed-pw", null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("userId");
	}

	@Test
	@DisplayName("userId가 같으면 같은 객체로 판단한다 (equals/hashCode).")
	void testEqualsAndHashCode() {
		User user1 = new User("same-id", "user1", "hash1", null);
		User user2 = new User("same-id", "user2", "hash2", null);
		User user3 = new User("diff-id", "user1", "hash1", null);

		assertThat(user1).isEqualTo(user2);
		assertThat(user1).isNotEqualTo(user3);
		assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
		assertThat(user1.hashCode()).isNotEqualTo(user3.hashCode());
	}
}
