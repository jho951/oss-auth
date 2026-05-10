package com.auth.support.jwt.spi;

import java.security.Key;
import java.util.Optional;

/** 토큰 헤더의 `kid` 값을 기준으로 검증 키를 조회합니다. */
public interface JwtKeyResolver {

	Optional<Key> resolve(String keyId);
}
