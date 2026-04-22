package com.auth.api.model;

import java.util.List;
import java.util.Objects;
import java.util.Collections;
import com.auth.common.utils.Strings;

/**
 * 인증 전용 사용자 정보 원본
 * UserEntity와 분리
 */
public final class User {
	/** DB에서 사용하는 고유 번호(PK) */
	private final String userId;
	/** 사용자가 로그인할 때 입력하는 아이디나 이메일 */
	private final String username;
	/** 보안을 위해 암호화된 비밀번호 (해시값) */
	private final String passwordHash;
	/** 주체가 어떤 권한을 가졌는지 담고 있는 목록 */
	private final List<String> authorities;

	public User(String userId, String username, String passwordHash, List<String> roles) {
		this.userId = Strings.requireNonBlank(userId, "userId");
		this.username = Strings.requireNonBlank(username, "username");
		this.passwordHash = Strings.requireNonBlank(passwordHash, "passwordHash");
		this.authorities = roles == null ? Collections.emptyList() : List.copyOf(roles);
	}

	public String getUserId() {
		return userId;
	}
	public String getUsername() {
		return username;
	}
	public String getPasswordHash() {
		return passwordHash;
	}
	public List<String> getAuthorities() {
		return authorities;
	}
	@Deprecated
	public List<String> getRoles() {return getAuthorities();}
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof User)) return false;
		User other = (User) o;
		return Objects.equals(userId, other.userId);
	}
	@Override
	public int hashCode() {return Objects.hash(userId);}
}
