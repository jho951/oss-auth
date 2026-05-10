package com.auth.core.spi.token;

import com.auth.core.api.model.Principal;

/** 리프레시 토큰 교체 전략 */
public interface RefreshTokenRotationStrategy {

	RefreshTokenRotation rotate(Principal principal, String currentRefreshToken);
}
