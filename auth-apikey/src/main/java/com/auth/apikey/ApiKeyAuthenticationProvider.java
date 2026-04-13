package com.auth.apikey;

import com.auth.api.authentication.AuthenticationProvider;
import com.auth.api.model.Principal;
import java.util.Objects;
import java.util.Optional;

/** Generic API key authentication provider. */
public final class ApiKeyAuthenticationProvider implements AuthenticationProvider<ApiKeyCredential> {

	private final ApiKeyPrincipalResolver resolver;

	public ApiKeyAuthenticationProvider(ApiKeyPrincipalResolver resolver) {
		this.resolver = Objects.requireNonNull(resolver, "resolver");
	}

	@Override
	public Optional<Principal> authenticate(ApiKeyCredential credential) {
		if (credential == null || credential.secret().isBlank()) return Optional.empty();
		return resolver.resolve(credential);
	}
}

