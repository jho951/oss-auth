package com.auth.core.api.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** 사용자가 가진 권한 (보안을 위해 Immutable) */
public final class AuthoritySet implements Serializable {

	private static final long serialVersionUID = 1L;

	private final List<String> authorities;

	/**
	 * 함부로 수정하지 못하게 하고 데이터를 깨끗하게 정리
	 * @param authorities
	 */
	private AuthoritySet(Collection<String> authorities) {
		this.authorities = authorities == null
			? com.auth.core.utils.CollectionUtils.listOf()
			: com.auth.core.utils.CollectionUtils.copyList(authorities.stream()
				.filter(Objects::nonNull)
				.map(String::trim)
				.filter(value -> !value.isEmpty())
				.distinct()
				.collect(Collectors.toList()));
	}

	public static AuthoritySet empty() {return new AuthoritySet(com.auth.core.utils.CollectionUtils.listOf());}
	public static AuthoritySet of(Collection<String> authorities) {return new AuthoritySet(authorities);}
	public List<String> asList() {return authorities;}
	public boolean contains(String authority) {return authorities.contains(authority);}
	public boolean isEmpty() {return authorities.isEmpty();}
}
