package com.auth.support.jwt.spi;

import com.auth.core.utils.Strings;
import java.security.Key;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** 사용할 검증 키를 메모리에 고정해 두는 resolver입니다. */
public final class StaticJwtKeyResolver implements JwtKeyResolver {

	/** 기본 검증 키입니다. */
	private final Key defaultKey;
	/** `kid`별로 보관하는 검증 키입니다. */
	private final Map<String, Key> keysById;

	public StaticJwtKeyResolver(Key defaultKey) {
		this(defaultKey, com.auth.core.utils.CollectionUtils.mapOf());
	}

	public StaticJwtKeyResolver(Key defaultKey, Map<String, Key> keysById) {
		this.defaultKey = Objects.requireNonNull(defaultKey, "defaultKey");
		this.keysById = keysById == null ? com.auth.core.utils.CollectionUtils.mapOf() : com.auth.core.utils.CollectionUtils.copyMap(keysById);
	}

	@Override
	public Optional<Key> resolve(String keyId) {
		if (Strings.isBlank(keyId)) return Optional.of(defaultKey);
		return Optional.ofNullable(keysById.get(keyId));
	}
}
