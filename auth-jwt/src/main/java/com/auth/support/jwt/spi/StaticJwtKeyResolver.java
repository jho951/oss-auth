package com.auth.support.jwt.spi;

import java.security.Key;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Verification resolver backed by an immutable in-memory key map. */
public final class StaticJwtKeyResolver implements JwtKeyResolver {

	private final Key defaultKey;
	private final Map<String, Key> keysById;

	public StaticJwtKeyResolver(Key defaultKey) {
		this(defaultKey, Map.of());
	}

	public StaticJwtKeyResolver(Key defaultKey, Map<String, Key> keysById) {
		this.defaultKey = Objects.requireNonNull(defaultKey, "defaultKey");
		this.keysById = keysById == null ? Map.of() : Map.copyOf(keysById);
	}

	@Override
	public Optional<Key> resolve(String keyId) {
		if (keyId == null || keyId.isBlank()) return Optional.of(defaultKey);
		return Optional.ofNullable(keysById.get(keyId));
	}
}
