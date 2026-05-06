package com.auth.spi;


import com.auth.api.model.Principal;

/** access token과 refresh token의 발급 및 검증을 담당하는 포트입니다. */
public interface TokenService {

	/** Access Token 발급 */
	String issueAccessToken(Principal principal);

	/**
	 * Refresh Token 발급
	 * - "문자열"만 반환 (쿠키로 굽는 건 웹 어댑터 책임)
	 */
	String issueRefreshToken(Principal principal);

	/**
	 * Access Token 검증 -> Principal 추출
	 * @throws RuntimeException if invalid/expired
	 */
	Principal verifyAccessToken(String token);

	/** Refresh Token 검증 -> Principal 추출 */
	Principal verifyRefreshToken(String token);
}
