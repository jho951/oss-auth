package com.auth.spi.token;

import java.time.Instant;

/** Store for token revocation metadata such as jti or opaque token identifiers. */
public interface TokenRevocationStore {

	void revoke(String tokenId, Instant expiresAt);

	boolean isRevoked(String tokenId);
}
