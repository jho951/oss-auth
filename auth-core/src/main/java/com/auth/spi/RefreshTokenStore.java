package com.auth.spi;

import java.time.Instant;

/** 서버 기준 refresh token 상태를 저장하고 조회하는 포트입니다. */
public interface RefreshTokenStore {

	/**
	 * refreshToken을 저장한다.
	 * userId별로 "현재 유효한 refresh"를 1개로 제한할지,
	 *  여러 개를 허용할지는 구현체 정책으로 결정 가능
	 */
	void save(String userId, String refreshToken, Instant expiresAt);

	/**
	 * refreshToken이 저장소 기준으로 유효한지 확인한다.
	 * 토큰 자체의 서명/만료는 TokenService가 검증
	 * 여긴 "서버에서 폐기된 토큰인지"를 확인하는 용도
	 */
	boolean exists(String userId, String refreshToken);

	/** 로그아웃/강제 만료 시 폐기 */
	void revoke(String userId, String refreshToken);
}
