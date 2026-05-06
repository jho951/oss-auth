package com.auth.spi.token;

import com.auth.api.model.Principal;

/** 리프레시 토큰 교체 전략 (서비스마다 정책이 달라 인터페이스로 구현) */
public interface RefreshTokenRotationStrategy {

	RefreshTokenRotation rotate(Principal principal, String currentRefreshToken);
}
