package com.auth.core.spi;

import com.auth.core.api.model.User;
import com.auth.core.api.exception.AuthFailureReason;

import java.util.Optional;

/** 사용자의 인증 가능 상태를 판정하는 포트입니다. */
public interface UserStatusChecker {

	Optional<AuthFailureReason> check(User user);

	static UserStatusChecker allowAll() {
		return user -> Optional.empty();
	}
}
