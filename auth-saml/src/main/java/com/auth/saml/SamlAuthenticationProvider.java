package com.auth.saml;

import com.auth.core.api.authentication.AuthenticationProvider;
import com.auth.core.api.model.Principal;
import com.auth.core.utils.Strings;
import java.util.Objects;
import java.util.Optional;

/** XML 서명 검증과 principal 매핑을 위임하는 범용 SAML 인증 provider입니다. */
public final class SamlAuthenticationProvider implements AuthenticationProvider<SamlAuthenticationRequest> {

	private final SamlResponseVerifier responseVerifier;
	private final SamlAssertionValidator assertionValidator;
	private final SamlPrincipalMapper principalMapper;

	public SamlAuthenticationProvider(
		SamlResponseVerifier responseVerifier,
		SamlAssertionValidator assertionValidator,
		SamlPrincipalMapper principalMapper
	) {
		this.responseVerifier = Objects.requireNonNull(responseVerifier, "responseVerifier");
		this.assertionValidator = assertionValidator == null ? new DefaultSamlAssertionValidator() : assertionValidator;
		this.principalMapper = Objects.requireNonNull(principalMapper, "principalMapper");
	}

	@Override
	public Optional<Principal> authenticate(SamlAuthenticationRequest request) {
		if (request == null || Strings.isBlank(request.getResponseXml())) return Optional.empty();
		SamlAssertion assertion = responseVerifier.verify(request);
		assertionValidator.validate(assertion, request);
		return Optional.of(principalMapper.map(assertion));
	}
}
