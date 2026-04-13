package com.auth.hybrid;

import com.auth.api.authentication.AuthenticationSource;
import com.auth.api.model.Principal;
import java.util.Optional;

/** Result of trying one source inside hybrid authentication. */
public record HybridAuthenticationAttempt(AuthenticationSource source, Optional<Principal> principal) {

	public static HybridAuthenticationAttempt empty(AuthenticationSource source) {
		return new HybridAuthenticationAttempt(source, Optional.empty());
	}
}
