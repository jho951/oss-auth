package com.auth.api.authentication;

import com.auth.api.model.Principal;
import java.util.Optional;

/**
 * Generic authentication provider contract for a single credential type.
 *
 * @param <C> credential/context type consumed by this provider
 */
public interface AuthenticationProvider<C> {

	Optional<Principal> authenticate(C credential);
}
