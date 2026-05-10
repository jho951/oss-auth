package com.auth.support.jwt;

import com.auth.core.api.exception.AuthException;
import com.auth.core.api.exception.AuthFailureReason;
import com.auth.core.api.model.Principal;
import com.auth.support.jwt.spi.JwtClaimsMapper;
import io.jsonwebtoken.Claims;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** {@link JwtTokenService}가 사용하는 기본 claim 스키마 매퍼입니다. */
public final class DefaultJwtClaimsMapper implements JwtClaimsMapper {

	public static final String KEY_TOKEN_TYPE = "token_type";
	public static final String KEY_AUTHORITIES = "authorities";

	@Override
	public Map<String, Object> toClaims(Principal principal) {
		Map<String, Object> claims = new HashMap<>(principal.getAttributes());
		if (!principal.getAuthorities().isEmpty()) {
			claims.put(KEY_AUTHORITIES, principal.getAuthorities());
		}
		return claims;
	}

	@Override
	public Principal toPrincipal(Claims claims, String expectedType) {
		String type = claims.get(KEY_TOKEN_TYPE, String.class);
		if (type == null || !type.equals(expectedType)) {
			throw new AuthException(AuthFailureReason.INVALID_TOKEN, "invalid token type");
		}

		Map<String, Object> attributes = new HashMap<>(claims);
		attributes.remove(Claims.SUBJECT);
		attributes.remove(Claims.ISSUED_AT);
		attributes.remove(Claims.EXPIRATION);
		attributes.remove(KEY_TOKEN_TYPE);
		List<String> authorities = toAuthorities(attributes.remove(KEY_AUTHORITIES));
		return new Principal(claims.getSubject(), authorities, attributes);
	}

	private List<String> toAuthorities(Object rawAuthorityData) {
		if (rawAuthorityData instanceof List<?> list) {
			return list.stream()
				.filter(Objects::nonNull)
				.map(Object::toString)
				.toList();
		}
		if (rawAuthorityData instanceof String value) return List.of(value);
		return List.of();
	}
}
