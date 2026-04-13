package com.auth.hybrid.strategy;

import com.auth.api.authentication.AuthenticationSource;
import com.auth.api.model.Principal;

/** Resolves the case where more than one authentication source succeeds. */
public interface HybridConflictResolver {

	Principal resolve(AuthenticationSource preferredSource, Principal preferred, AuthenticationSource otherSource, Principal other);
}
