package com.auth.api.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/** Immutable set-like authority value object that preserves declaration order. */
public final class AuthoritySet implements Serializable {

	private static final long serialVersionUID = 1L;

	private final List<String> authorities;

	private AuthoritySet(Collection<String> authorities) {
		this.authorities = authorities == null
			? List.of()
			: authorities.stream()
				.filter(Objects::nonNull)
				.map(String::trim)
				.filter(value -> !value.isEmpty())
				.distinct()
				.toList();
	}

	public static AuthoritySet empty() {
		return new AuthoritySet(List.of());
	}

	public static AuthoritySet of(Collection<String> authorities) {
		return new AuthoritySet(authorities);
	}

	public List<String> asList() {
		return authorities;
	}

	public boolean contains(String authority) {
		return authorities.contains(authority);
	}

	public boolean isEmpty() {
		return authorities.isEmpty();
	}
}
