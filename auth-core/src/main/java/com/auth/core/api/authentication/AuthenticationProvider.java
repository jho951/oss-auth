package com.auth.core.api.authentication;

import com.auth.core.api.model.Principal;

import java.util.Optional;

/**
 * 입력값(C)을 검증해서 Principal를 제공
 * @param <C> JWT, ID/PW, API Key, HMAC 서명
 */
public interface AuthenticationProvider<C> {

	Optional<Principal> authenticate(C credential);
}
