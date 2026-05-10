package com.auth.session;

import static org.assertj.core.api.Assertions.assertThat;

import com.auth.core.api.model.Principal;
import com.auth.session.id.SessionIdGenerator;
import com.auth.session.store.SessionStore;
import com.auth.session.store.TestSessionStore;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SessionServiceTest {

	private final SessionStore store = new TestSessionStore();
	private final SessionIdGenerator generator = () -> "session-42";
	private final SessionService service = new SessionService(store, generator, Duration.ofMinutes(30));

	@Test
	@DisplayName("세션을 생성하면 고정된 식별자로 저장된다")
	void createSessionStoresPrincipal() {
		String sessionId = service.create(principal("user-1"));

		assertThat(sessionId).isEqualTo("session-42");
		assertThat(service.resolve(sessionId))
			.map(Principal::getUserId)
			.contains("user-1");
	}

	@Test
	@DisplayName("세션을 폐기하면 더 이상 조회되지 않는다")
	void revokeSessionRemovesPrincipal() {
		String sessionId = service.create(principal("user-1"));
		service.revoke(sessionId);

		assertThat(service.resolve(sessionId)).isEmpty();
	}

	private static Principal principal(String userId) {
		return new Principal(userId);
	}
}
