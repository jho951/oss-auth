package com.auth.hybrid.strategy;

import com.auth.api.authentication.AuthenticationSource;
import com.auth.api.exception.AuthException;
import com.auth.api.exception.AuthFailureReason;
import com.auth.api.model.Principal;

/** Allows multiple successful sources only when they identify the same principal. */
public final class MatchingPrincipalConflictResolver implements HybridConflictResolver {

	@Override
	public Principal resolve(AuthenticationSource preferredSource, Principal preferred, AuthenticationSource otherSource, Principal other) {
		if (preferred.getUserId().equals(other.getUserId())) {
			return preferred;
		}
		throw new AuthException(AuthFailureReason.INVALID_TOKEN, "conflicting authentication principals");
	}
}
