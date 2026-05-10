package com.auth.support.jwt.spi;

import com.auth.core.api.model.Principal;
import io.jsonwebtoken.Claims;
import java.util.Map;

/** `Principal` 객체와 JWT claim 데이터를 서로 변환합니다. */
public interface JwtClaimsMapper {

	Map<String, Object> toClaims(Principal principal);

	Principal toPrincipal(Claims claims, String expectedType);
}
