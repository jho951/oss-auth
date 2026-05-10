package com.auth.apikey;

import com.auth.core.api.authentication.AuthenticationProvider;
import com.auth.core.api.model.Principal;
import java.util.Objects;
import java.util.Optional;

/** API-key 사용자의 신원을 확인(인증) */
public final class ApiKeyAuthenticationProvider implements AuthenticationProvider<ApiKeyCredential> {

	private final ApiKeyPrincipalResolver resolver;

	public ApiKeyAuthenticationProvider(ApiKeyPrincipalResolver resolver) {
		this.resolver = Objects.requireNonNull(resolver, "resolver");
	}

	@Override
	public Optional<Principal> authenticate(ApiKeyCredential credential) {
		if (credential == null) return Optional.empty();
        if (credential.secret().isBlank()) return Optional.empty();
		return resolver.resolve(credential);
	}
}

