package com.auth.api.model;

import com.auth.api.exception.AuthException;
import com.auth.api.exception.AuthFailureReason;
import com.auth.common.utils.Strings;

/**
 * 로그인에 성공 시 제공 토큰
 * 웹(스프링)에서는 refresh를 쿠키로 굽거나 바디로 내릴 수 있음
 * core는 "문자열"만 제공하고, 쿠키 처리는 컨트롤러가 담당
 */
public final class Tokens {
	/** 실제 서비스를 이용할 때 서버에 보여주는 토큰 */
	private final String accessToken;
	/** AccessToken이 만료되었을 때, 갱신 토큰 */
	private final String refreshToken;

	public Tokens(String accessToken, String refreshToken) {
		if (Strings.isBlank(accessToken)) throw new AuthException(AuthFailureReason.INVALID_INPUT, "accessToken must not be blank");
		if (Strings.isBlank(refreshToken)) throw new AuthException(AuthFailureReason.INVALID_INPUT, "refreshToken must not be blank");
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
	}

	public String getAccessToken() {
		return accessToken;
	}
	public String getRefreshToken() {
		return refreshToken;
	}
}
