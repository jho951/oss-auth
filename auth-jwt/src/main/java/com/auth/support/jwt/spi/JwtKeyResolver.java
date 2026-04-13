package com.auth.support.jwt.spi;

import java.security.Key;
import java.util.Optional;

/** Resolves verification keys by JWT kid header. */
public interface JwtKeyResolver {

	Optional<Key> resolve(String keyId);
}
