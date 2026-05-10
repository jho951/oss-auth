package com.auth.core.spi.token;

import java.time.Instant;

/** 특정 토큰을 강제로 무효화(블랙리스트 처리) */
public interface TokenRevocationStore {

	void revoke(String tokenId, Instant expiresAt);

	boolean isRevoked(String tokenId);
}
