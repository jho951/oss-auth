package com.auth.spi;

/** 사용자 입력 비밀번호와 저장된 해시를 비교하는 포트입니다. */
public interface PasswordVerifier {
	/** rawPassword가 storedHash와 매칭되는지 검증 */
	boolean matches(String rawPassword, String storedHash);
}
