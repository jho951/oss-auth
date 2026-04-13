package com.auth.hmac;

import com.auth.api.model.Principal;
import java.util.Optional;

/** Resolves a verified HMAC key id to a principal. */
public interface HmacPrincipalResolver {

	Optional<Principal> resolvePrincipal(String keyId);
}
