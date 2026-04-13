package com.auth.oidc;

import com.auth.api.authentication.AuthenticationProvider;
import com.auth.api.model.Principal;
import java.util.Objects;
import java.util.Optional;

/** Generic OIDC authentication provider. */
public final class OidcAuthenticationProvider implements AuthenticationProvider<OidcAuthenticationRequest> {

	private final OidcTokenVerifier tokenVerifier;
	private final OidcPrincipalMapper principalMapper;

	public OidcAuthenticationProvider(OidcTokenVerifier tokenVerifier, OidcPrincipalMapper principalMapper) {
		this.tokenVerifier = Objects.requireNonNull(tokenVerifier, "tokenVerifier");
		this.principalMapper = Objects.requireNonNull(principalMapper, "principalMapper");
	}

	@Override
	public Optional<Principal> authenticate(OidcAuthenticationRequest request) {
		if (request == null || request.idToken() == null || request.idToken().isBlank()) return Optional.empty();
		return Optional.of(principalMapper.map(tokenVerifier.verify(request)));
	}
}
