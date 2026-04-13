package com.auth.hybrid.strategy;

import com.auth.api.authentication.AuthenticationSource;
import com.auth.api.model.Principal;

/** Conflict resolver that keeps the source selected by the resolution strategy. */
public final class PreferFirstConflictResolver implements HybridConflictResolver {

	@Override
	public Principal resolve(AuthenticationSource preferredSource, Principal preferred, AuthenticationSource otherSource, Principal other) {
		return preferred;
	}
}
