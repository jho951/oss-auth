package com.auth.spi.token;

import com.auth.api.model.Principal;

/** Strategy for rotating refresh tokens without coupling to a specific store. */
public interface RefreshTokenRotationStrategy {

	RefreshTokenRotation rotate(Principal principal, String currentRefreshToken);
}
