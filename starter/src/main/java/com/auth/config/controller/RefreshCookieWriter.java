package com.auth.config.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

import com.auth.api.model.Tokens;
import com.auth.config.AuthProperties;
import com.auth.config.jwt.AuthJwtProperties;

/**
 * Refresh Token 쿠키 생성 및 응답 헤더 설정을 담당합니다.
 */
public class RefreshCookieWriter {

	private final AuthProperties props;
	private final AuthJwtProperties jwtProps;

	public RefreshCookieWriter(AuthProperties props, AuthJwtProperties jwtProps) {
		this.props = props;
		this.jwtProps = jwtProps;
	}

	public <T> ResponseEntity<T> write(Tokens tokens, ResponseEntity<T> base) {
		if (!props.isRefreshCookieEnabled()) return base;

		ResponseCookie cookie = ResponseCookie.from(props.getRefreshCookieName(), tokens.getRefreshToken())
			.httpOnly(props.isRefreshCookieHttpOnly())
			.secure(props.isRefreshCookieSecure())
			.path(props.getRefreshCookiePath())
			.maxAge(jwtProps.getRefreshSeconds())
			.sameSite(props.getRefreshCookieSameSite())
			.build();

		return ResponseEntity.status(base.getStatusCode())
			.header(HttpHeaders.SET_COOKIE, cookie.toString())
			.body(base.getBody());
	}

	public ResponseEntity<Void> clear(ResponseEntity<Void> base) {
		if (!props.isRefreshCookieEnabled()) return base;

		ResponseCookie cleared = ResponseCookie.from(props.getRefreshCookieName(), "")
			.httpOnly(props.isRefreshCookieHttpOnly())
			.secure(props.isRefreshCookieSecure())
			.path(props.getRefreshCookiePath())
			.maxAge(0)
			.sameSite(props.getRefreshCookieSameSite())
			.build();

		return ResponseEntity.status(base.getStatusCode())
			.header(HttpHeaders.SET_COOKIE, cleared.toString())
			.build();
	}
}
