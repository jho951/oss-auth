package com.auth.api.model.subject;

import static org.assertj.core.api.Assertions.assertThat;

import com.auth.api.model.AuthoritySet;
import com.auth.api.model.Principal;
import com.auth.api.model.PrincipalType;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AuthenticatedSubjectTest {

	@Test
	void preservesPrincipalDataWithoutServicePolicy() {
		Principal principal = new Principal("user-1", List.of("READ"), Map.of("tenant", "t1"));

		AuthenticatedSubject subject = AuthenticatedSubject.fromPrincipal(principal, PrincipalType.USER);

		assertThat(subject.getId()).isEqualTo("user-1");
		assertThat(subject.getType()).isEqualTo(PrincipalType.USER);
		assertThat(subject.getAuthorities().asList()).containsExactly("READ");
		assertThat(subject.toPrincipal().getAttributes()).containsEntry("tenant", "t1");
	}

	@Test
	void authoritySetRemovesBlankAndDuplicateValues() {
		AuthoritySet authorities = AuthoritySet.of(List.of("READ", " ", "READ", "WRITE"));

		assertThat(authorities.asList()).containsExactly("READ", "WRITE");
	}
}
