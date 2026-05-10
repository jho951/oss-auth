package com.auth.support.jwt.spi;

import java.util.UUID;

/** 무작위 UUID를 사용하는 기본 `jti` 생성기입니다. */
public final class RandomUuidJwtIdGenerator implements JwtIdGenerator {

	@Override
	public String generateId() {
		return UUID.randomUUID().toString();
	}
}
