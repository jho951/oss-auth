package com.auth.api.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.auth.core.api.exception.AuthException;
import com.auth.core.api.exception.AuthFailureReason;

class AuthExceptionTest {

	@Test
	@DisplayName("reason만 인자로 전달할 경우, reason 이름을 예외 메시지로 사용한다.")
	void authException_ConstructorWithOnlyReason() {
		AuthFailureReason reason = AuthFailureReason.INVALID_TOKEN;

		AuthException exception = new AuthException(reason);

		assertThat(exception.getMessage()).isEqualTo(reason.name());
		assertThat(exception.getReason()).isEqualTo(reason);
	}

	@Test
	@DisplayName("사용자 정의 메시지를 함께 전달할 경우, 기본 메시지 대신 전달된 메시지를 사용한다.")
	void authException_ConstructorWithCustomMessage() {
		AuthFailureReason reason = AuthFailureReason.INVALID_TOKEN;
		String customMessage = "토큰의 서명이 유효하지 않습니다.";

		AuthException exception = new AuthException(reason, customMessage);

		assertThat(exception.getMessage()).isEqualTo(customMessage);
		assertThat(exception.getReason()).isEqualTo(reason);
	}

	@Test
	@DisplayName("원인 예외(Throwable cause)가 포함되어도 reason 정보를 유지한다.")
	void authException_WithCause() {
		RuntimeException cause = new RuntimeException("Original error");
		AuthException exception = new AuthException(AuthFailureReason.INTERNAL, cause);

		assertThat(exception.getCause()).isEqualTo(cause);
		assertThat(exception.getReason()).isEqualTo(AuthFailureReason.INTERNAL);
	}

	@Test
	@DisplayName("toString() 호출 시 reason과 메시지를 포함한다.")
	void authException_ToString() {
		AuthException exception = new AuthException(AuthFailureReason.USER_NOT_FOUND, "유저를 찾을 수 없음");

		assertThat(exception.toString())
			.contains("USER_NOT_FOUND")
			.contains("유저를 찾을 수 없음");
	}
}
