package com.auth.support.jwt.spi;

/** JWT 발급 시 사용할 토큰 식별자를 생성합니다. */
public interface JwtIdGenerator {

	String generateId();
}
