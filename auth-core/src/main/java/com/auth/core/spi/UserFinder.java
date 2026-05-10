package com.auth.core.spi;

import com.auth.core.api.model.User;
import java.util.Optional;

/** username 기반으로 인증 대상 사용자를 조회하는 포트입니다. */
public interface UserFinder {
	/**
	 * username(아이디/이메일 등 로그인 식별자)으로 유저를 조회한다.
	 * @return 유저가 없으면 Optional.empty()
	 */
	Optional<User> findByUsername(String username);
}
