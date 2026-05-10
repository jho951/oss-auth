package com.auth.webauthn;

import com.auth.core.api.authentication.AuthenticationProvider;
import com.auth.core.api.model.Principal;
import java.util.Objects;
import java.util.Optional;

/** 저장된 passkey credential을 먼저 조회한 뒤 assertion을 검증하는 범용 provider입니다. */
public final class PasskeyAuthenticationProvider implements AuthenticationProvider<WebAuthnAuthenticationRequest> {

	private final WebAuthnCredentialResolver credentialResolver;
	private final WebAuthnAssertionVerifier assertionVerifier;
	private final PasskeyPrincipalMapper principalMapper;
	private final DefaultWebAuthnRequestValidator requestValidator;

	public PasskeyAuthenticationProvider(
		WebAuthnCredentialResolver credentialResolver,
		WebAuthnAssertionVerifier assertionVerifier,
		PasskeyPrincipalMapper principalMapper
	) {
		this(credentialResolver, assertionVerifier, principalMapper, new DefaultWebAuthnRequestValidator());
	}

	public PasskeyAuthenticationProvider(
		WebAuthnCredentialResolver credentialResolver,
		WebAuthnAssertionVerifier assertionVerifier,
		PasskeyPrincipalMapper principalMapper,
		DefaultWebAuthnRequestValidator requestValidator
	) {
		this.credentialResolver = Objects.requireNonNull(credentialResolver, "credentialResolver");
		this.assertionVerifier = Objects.requireNonNull(assertionVerifier, "assertionVerifier");
		this.principalMapper = Objects.requireNonNull(principalMapper, "principalMapper");
		this.requestValidator = requestValidator == null ? new DefaultWebAuthnRequestValidator() : requestValidator;
	}

	@Override
	public Optional<Principal> authenticate(WebAuthnAuthenticationRequest request) {
		if (!requestValidator.isValid(request)) return Optional.empty();
		return credentialResolver.resolve(request.getCredentialId())
			.flatMap(record -> assertionVerifier.verify(request, record)
				.map(assertionResult -> principalMapper.map(assertionResult, record)));
	}
}
