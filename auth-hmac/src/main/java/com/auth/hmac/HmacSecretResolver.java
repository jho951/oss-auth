package com.auth.hmac;

import java.util.Optional;

/** Resolves HMAC shared secrets by key id. */
public interface HmacSecretResolver {

	Optional<byte[]> resolveSecret(String keyId);
}
