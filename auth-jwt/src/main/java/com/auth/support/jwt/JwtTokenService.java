package com.auth.support.jwt;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Duration;
import java.util.Date;
import java.util.Objects;

import com.auth.core.api.exception.AuthException;
import com.auth.core.api.exception.AuthFailureReason;
import com.auth.core.api.model.Principal;
import com.auth.core.spi.TokenService;
import com.auth.core.spi.token.TokenRevocationStore;
import com.auth.core.utils.Strings;
import com.auth.support.jwt.spi.JwtClaimsMapper;
import com.auth.support.jwt.spi.JwtIdGenerator;
import com.auth.support.jwt.spi.JwtKeyResolver;
import com.auth.support.jwt.spi.JwtSigningKeyProvider;
import com.auth.support.jwt.spi.RandomUuidJwtIdGenerator;
import com.auth.support.jwt.spi.StaticJwtKeyResolver;
import com.auth.support.jwt.spi.StaticJwtSigningKeyProvider;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SigningKeyResolverAdapter;
import io.jsonwebtoken.security.Keys;

public class JwtTokenService implements TokenService {

	private final JwtSigningKeyProvider signingKeyProvider;
	private final JwtKeyResolver keyResolver;
	private final JwtClaimsMapper claimsMapper;
	private final JwtTokenOptions options;
	private final TokenRevocationStore tokenRevocationStore;
	private final JwtIdGenerator jwtIdGenerator;

	private String buildToken(Principal principal, long ttlSeconds, String type) {
		Date now = new Date();
		Date exp = new Date(now.getTime() + (ttlSeconds * 1000L));

		JwtBuilder builder = Jwts.builder()
			.setSubject(principal.getUserId())
			.addClaims(claimsMapper.toClaims(principal))
			.claim(DefaultJwtClaimsMapper.KEY_TOKEN_TYPE, type)
			.setId(jwtIdGenerator.generateId())
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

	public JwtTokenService(String secret, long accessSeconds, long refreshSeconds) {
		this(secret, accessSeconds, refreshSeconds, null);
	}

	public JwtTokenService(String secret, long accessSeconds, long refreshSeconds, TokenRevocationStore tokenRevocationStore) {
		if (Strings.isBlank(secret)) throw new AuthException(AuthFailureReason.INVALID_INPUT, "auth.jwt.secret must not be blank");
		byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
		if (bytes.length < 32) throw new AuthException(AuthFailureReason.INVALID_INPUT, "auth.jwt.secret must be at least 32 bytes for HS256");

		Key key = Keys.hmacShaKeyFor(bytes);
		this.signingKeyProvider = new StaticJwtSigningKeyProvider(key);
		this.keyResolver = new StaticJwtKeyResolver(key);
		this.claimsMapper = new DefaultJwtClaimsMapper();
		this.options = JwtTokenOptions.hs256(accessSeconds, refreshSeconds);
		this.tokenRevocationStore = tokenRevocationStore;
		this.jwtIdGenerator = new RandomUuidJwtIdGenerator();
	}

	public JwtTokenService(PrivateKey signingKey, PublicKey verificationKey, long accessSeconds, long refreshSeconds) {
		this(
			new StaticJwtSigningKeyProvider(Objects.requireNonNull(signingKey, "signingKey")),
			new StaticJwtKeyResolver(Objects.requireNonNull(verificationKey, "verificationKey")),
			new DefaultJwtClaimsMapper(),
			new JwtTokenOptions(Duration.ofSeconds(accessSeconds), Duration.ofSeconds(refreshSeconds), SignatureAlgorithm.RS256, null),
			null,
			new RandomUuidJwtIdGenerator()
		);
	}

	public JwtTokenService(
		JwtSigningKeyProvider signingKeyProvider,
		JwtKeyResolver keyResolver,
		JwtClaimsMapper claimsMapper,
		JwtTokenOptions options
	) {
		this(signingKeyProvider, keyResolver, claimsMapper, options, null, new RandomUuidJwtIdGenerator());
	}

	public JwtTokenService(
		JwtSigningKeyProvider signingKeyProvider,
		JwtKeyResolver keyResolver,
		JwtClaimsMapper claimsMapper,
		JwtTokenOptions options,
		TokenRevocationStore tokenRevocationStore,
		JwtIdGenerator jwtIdGenerator
	) {
		this.signingKeyProvider = Objects.requireNonNull(signingKeyProvider, "signingKeyProvider");
		this.keyResolver = Objects.requireNonNull(keyResolver, "keyResolver");
		this.claimsMapper = claimsMapper == null ? new DefaultJwtClaimsMapper() : claimsMapper;
		this.options = Objects.requireNonNull(options, "options");
		this.tokenRevocationStore = tokenRevocationStore;
		this.jwtIdGenerator = jwtIdGenerator == null ? new RandomUuidJwtIdGenerator() : jwtIdGenerator;
	}

	private Claims parseClaims(String token, boolean checkRevocation) {
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
			if (checkRevocation && tokenRevocationStore != null && claims.getId() != null && tokenRevocationStore.isRevoked(claims.getId())) {
				throw new AuthException(AuthFailureReason.REVOKED_TOKEN, "revoked token");
			}
			return claims;
		} catch (JwtException | IllegalArgumentException e) {
			throw new AuthException(AuthFailureReason.INVALID_TOKEN, "invalid/expired token", e);
		}
	}

	private Principal parseAndToPrincipal(String token, String expectedType) {
		return claimsMapper.toPrincipal(parseClaims(token, true), expectedType);
	}

	@Override
	public String issueAccessToken(Principal principal) {
		return buildToken(principal, options.getAccessTtl().getSeconds(), "access");
	}

	@Override
	public String issueRefreshToken(Principal principal) {
		return buildToken(principal, options.getRefreshTtl().getSeconds(), "refresh");
	}

	@Override
	public Principal verifyAccessToken(String token) {
		return parseAndToPrincipal(token, "access");
	}

	@Override
	public Principal verifyRefreshToken(String token) {
		return parseAndToPrincipal(token, "refresh");
	}

	public void revokeAccessToken(String token) {
		revoke(token, "access");
	}

	public void revokeRefreshToken(String token) {
		revoke(token, "refresh");
	}

	private void revoke(String token, String expectedType) {
		if (tokenRevocationStore == null) throw new IllegalStateException("tokenRevocationStore is not configured");
		Claims claims = parseClaims(token, false);
		claimsMapper.toPrincipal(claims, expectedType);
		if (claims.getId() == null) {
			throw new AuthException(AuthFailureReason.INVALID_TOKEN, "token is missing jti");
		}
		tokenRevocationStore.revoke(claims.getId(), claims.getExpiration().toInstant());
	}
}
