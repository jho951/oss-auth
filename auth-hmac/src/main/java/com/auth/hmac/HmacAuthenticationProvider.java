package com.auth.hmac;

import com.auth.api.authentication.AuthenticationProvider;
import com.auth.api.model.Principal;
import java.util.Objects;
import java.util.Optional;

/** Generic HMAC authentication provider. */
public final class HmacAuthenticationProvider implements AuthenticationProvider<HmacAuthenticationRequest> {

	private final HmacSecretResolver secretResolver;
	private final HmacSignatureVerifier signatureVerifier;
	private final HmacPrincipalResolver principalResolver;

	public HmacAuthenticationProvider(
		HmacSecretResolver secretResolver,
		HmacSignatureVerifier signatureVerifier,
		HmacPrincipalResolver principalResolver
	) {
		this.secretResolver = Objects.requireNonNull(secretResolver, "secretResolver");
		this.signatureVerifier = Objects.requireNonNull(signatureVerifier, "signatureVerifier");
		this.principalResolver = Objects.requireNonNull(principalResolver, "principalResolver");
	}

	@Override
	public Optional<Principal> authenticate(HmacAuthenticationRequest request) {
		if (request == null || request.keyId() == null || request.signature() == null) return Optional.empty();
		return secretResolver.resolveSecret(request.keyId())
			.filter(secret -> signatureVerifier.verify(request, secret))
			.flatMap(secret -> principalResolver.resolvePrincipal(request.keyId()));
	}
}
