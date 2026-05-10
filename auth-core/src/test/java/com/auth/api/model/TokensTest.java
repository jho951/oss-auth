package com.auth.api.model;

import com.auth.core.api.exception.AuthException;
import com.auth.core.api.exception.AuthFailureReason;
import com.auth.core.api.model.Tokens;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class TokensTest {

	@Test
	@DisplayName("정상적인 토큰 값이 전달되면 Tokens 객체가 생성된다.")
	void createTokens_Success() {
		String access = "access-token-123";
		String refresh = "refresh-token-456";

		Tokens tokens = new Tokens(access, refresh);

		assertThat(tokens.getAccessToken()).isEqualTo(access);
		assertThat(tokens.getRefreshToken()).isEqualTo(refresh);
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "  \n"})
	@DisplayName("AccessToken이 비어있으면 AuthException(AuthFailureReason.INVALID_INPUT)이 발생한다.")
	void createTokens_Fail_BlankAccessToken(String blankAccess) {
		assertThatThrownBy(() -> new Tokens(blankAccess, "refresh-token"))
			.isInstanceOf(AuthException.class)
			.hasFieldOrPropertyWithValue("reason", AuthFailureReason.INVALID_INPUT);
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "\t"})
	@DisplayName("RefreshToken이 비어있으면 AuthException(AuthFailureReason.INVALID_INPUT)이 발생한다.")
	void createTokens_Fail_BlankRefreshToken(String blankRefresh) {
		assertThatThrownBy(() -> new Tokens("access-token", blankRefresh))
			.isInstanceOf(AuthException.class)
			.hasFieldOrPropertyWithValue("reason", AuthFailureReason.INVALID_INPUT);
	}
}
