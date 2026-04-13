package com.auth.support.jwt;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Objects;

import com.auth.api.exception.AuthException;
import com.auth.api.exception.AuthFailureReason;
import com.auth.api.model.Principal;
import com.auth.common.utils.Strings;
import com.auth.spi.TokenService;
import com.auth.support.jwt.spi.JwtClaimsMapper;
import com.auth.support.jwt.spi.JwtKeyResolver;
import com.auth.support.jwt.spi.JwtSigningKeyProvider;
import com.auth.support.jwt.spi.StaticJwtKeyResolver;
import com.auth.support.jwt.spi.StaticJwtSigningKeyProvider;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SigningKeyResolverAdapter;
import io.jsonwebtoken.security.Keys;

public class JwtTokenService implements TokenService {

	private final JwtSigningKeyProvider signingKeyProvider;
	private final JwtKeyResolver keyResolver;
	private final JwtClaimsMapper claimsMapper;
	private final JwtTokenOptions options;

	private String buildToken(Principal principal, long ttlSeconds, String type) {
		Date now = new Date();
		Date exp = new Date(now.getTime() + (ttlSeconds * 1000L));

		var builder = Jwts.builder()
			.setSubject(principal.getUserId())
			.addClaims(claimsMapper.toClaims(principal))
			.claim(DefaultJwtClaimsMapper.KEY_TOKEN_TYPE, type)
			.setIssuedAt(now)
			.setExpiration(exp);

		String keyId = options.getKeyId() != null ? options.getKeyId() : signingKeyProvider.keyId();
		if (!Strings.isBlank(keyId)) {
			builder.setHeaderParam(JwsHeader.KEY_ID, keyId);
		}

		return builder
			.signWith(signingKeyProvider.signingKey(), options.getSignatureAlgorithm())
			.compact();
	}

	/**
	 * 생성자
	 * @param secret
	 * @param accessSeconds
	 * @param refreshSeconds
	 */
	public JwtTokenService(String secret, long accessSeconds, long refreshSeconds) {
		if (Strings.isBlank(secret)) throw new AuthException(AuthFailureReason.INVALID_INPUT, "auth.jwt.secret must not be blank");
		byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
		if (bytes.length < 32) throw new AuthException(AuthFailureReason.INVALID_INPUT, "auth.jwt.secret must be at least 32 bytes for HS256");

		Key key = Keys.hmacShaKeyFor(bytes);
		this.signingKeyProvider = new StaticJwtSigningKeyProvider(key);
		this.keyResolver = new StaticJwtKeyResolver(key);
		this.claimsMapper = new DefaultJwtClaimsMapper();
		this.options = JwtTokenOptions.hs256(accessSeconds, refreshSeconds);
	}

	public JwtTokenService(
		JwtSigningKeyProvider signingKeyProvider,
		JwtKeyResolver keyResolver,
		JwtClaimsMapper claimsMapper,
		JwtTokenOptions options
	) {
		this.signingKeyProvider = Objects.requireNonNull(signingKeyProvider, "signingKeyProvider");
		this.keyResolver = Objects.requireNonNull(keyResolver, "keyResolver");
		this.claimsMapper = claimsMapper == null ? new DefaultJwtClaimsMapper() : claimsMapper;
		this.options = Objects.requireNonNull(options, "options");
	}

	private Principal parseAndToPrincipal(String token, String expectedType) {
		try {
			Claims claims = Jwts.parserBuilder()
				.setSigningKeyResolver(new SigningKeyResolverAdapter() {
					@Override
					public Key resolveSigningKey(JwsHeader header, Claims claims) {
						String keyId = header == null ? null : header.getKeyId();
						return keyResolver.resolve(keyId)
							.orElseThrow(() -> new AuthException(AuthFailureReason.INVALID_TOKEN, "unknown signing key"));
					}
				})
				.build()
				.parseClaimsJws(token)
				.getBody();

			return claimsMapper.toPrincipal(claims, expectedType);
		} catch (JwtException | IllegalArgumentException e) {
			throw new AuthException(AuthFailureReason.INVALID_TOKEN, "invalid/expired token", e);
		}
	}

	@Override
	public String issueAccessToken(Principal principal) {
		return buildToken(principal, options.getAccessTtl().toSeconds(), "access");
	}
	@Override
	public String issueRefreshToken(Principal principal) {
		return buildToken(principal, options.getRefreshTtl().toSeconds(), "refresh");
	}
	@Override
	public Principal verifyAccessToken(String token) {
		return parseAndToPrincipal(token, "access");
	}
	@Override
	public Principal verifyRefreshToken(String token) {
		return parseAndToPrincipal(token, "refresh");
	}


}
