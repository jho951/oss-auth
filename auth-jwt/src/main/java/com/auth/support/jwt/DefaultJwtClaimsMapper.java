package com.auth.support.jwt;

import com.auth.api.exception.AuthException;
import com.auth.api.exception.AuthFailureReason;
import com.auth.api.model.Principal;
import com.auth.support.jwt.spi.JwtClaimsMapper;
import io.jsonwebtoken.Claims;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Default claim schema mapper used by {@link JwtTokenService}. */
public final class DefaultJwtClaimsMapper implements JwtClaimsMapper {

	public static final String KEY_TOKEN_TYPE = "token_type";
	public static final String KEY_AUTHORITIES = "authorities";
	public static final String KEY_ROLES = "roles";

	@Override
	public Map<String, Object> toClaims(Principal principal) {
		Map<String, Object> claims = new HashMap<>(principal.getAttributes());
		if (!principal.getAuthorities().isEmpty()) {
			claims.put(KEY_AUTHORITIES, principal.getAuthorities());
			claims.put(KEY_ROLES, principal.getAuthorities());
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
		if (authorities.isEmpty()) {
			authorities = toAuthorities(attributes.remove(KEY_ROLES));
		}
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
