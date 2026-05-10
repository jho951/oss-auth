package com.auth.serviceaccount;

import com.auth.core.api.authentication.AuthenticationProvider;
import com.auth.core.api.model.Principal;
import java.util.Objects;
import java.util.Optional;

/** 범용 서비스 계정 인증 provider입니다. */
public final class ServiceAccountAuthenticationProvider implements AuthenticationProvider<ServiceAccountCredential> {

	private final ServiceAccountVerifier verifier;

	public ServiceAccountAuthenticationProvider(ServiceAccountVerifier verifier) {
		this.verifier = Objects.requireNonNull(verifier, "verifier");
	}

	@Override
	public Optional<Principal> authenticate(ServiceAccountCredential credential) {
		if (credential == null || credential.serviceId() == null || credential.secret() == null) return Optional.empty();
		return verifier.verify(credential);
	}
}
