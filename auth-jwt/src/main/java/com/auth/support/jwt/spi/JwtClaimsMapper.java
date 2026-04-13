package com.auth.support.jwt.spi;

import com.auth.api.model.Principal;
import io.jsonwebtoken.Claims;
import java.util.Map;

/** Maps between generic principals and JWT claims. */
public interface JwtClaimsMapper {

	Map<String, Object> toClaims(Principal principal);

	Principal toPrincipal(Claims claims, String expectedType);
}
