package com.auth.support.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.Test;

import com.auth.core.api.exception.AuthException;
import com.auth.core.api.exception.AuthFailureReason;
import com.auth.core.api.model.Principal;
import com.auth.core.spi.token.TokenRevocationStore;
import com.auth.support.jwt.spi.JwksJwtKeyResolver;
import com.auth.support.jwt.spi.StaticJwtKeyResolver;
import com.auth.support.jwt.spi.StaticJwtSigningKeyProvider;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

class JwtTokenServiceTest {

	private static final String SECRET = "01234567890123456789012345678901";
	private final JwtTokenService tokenService = new JwtTokenService(SECRET, 60, 120);

	@Test
	void issueAccessToken_RoundsTripRolesAndAttributes() {
		Principal principal = new Principal("user-1", List.of("ADMIN"), Map.of("dept", "IT"));
		String token = tokenService.issueAccessToken(principal);

		Principal verified = tokenService.verifyAccessToken(token);

		assertThat(verified.getUserId()).isEqualTo("user-1");
		assertThat(verified.getAuthorities()).containsExactly("ADMIN");
		assertThat(verified.getAttributes()).containsEntry("dept", "IT");
	}

	@Test
	void accessTokenWithoutRolesReturnsEmptyList() {
		Principal principal = new Principal("user-2");
		String token = tokenService.issueAccessToken(principal);

		assertThat(tokenService.verifyAccessToken(token).getAuthorities()).isEmpty();
	}

	@Test
	void issueAccessToken_WritesAuthoritiesClaimAndJti() {
		Principal principal = new Principal("user-1", List.of("ADMIN"), Map.of());
		String token = tokenService.issueAccessToken(principal);

		Claims claims = parseClaims(token);
		assertThat(claims.get("authorities")).isEqualTo(List.of("ADMIN"));
		assertThat(claims.getId()).isNotBlank();
	}

	@Test
	void supportsKidBasedVerification() {
		Key oldKey = Keys.hmacShaKeyFor("old-012345678901234567890123456789".getBytes(StandardCharsets.UTF_8));
		Key newKey = Keys.hmacShaKeyFor("new-012345678901234567890123456789".getBytes(StandardCharsets.UTF_8));
		JwtTokenService rotatingService = new JwtTokenService(
			new StaticJwtSigningKeyProvider(newKey, "new"),
			new StaticJwtKeyResolver(oldKey, Map.of("new", newKey)),
			new DefaultJwtClaimsMapper(),
			new JwtTokenOptions(Duration.ofMinutes(1), Duration.ofMinutes(2), SignatureAlgorithm.HS256, null)
		);

		String token = rotatingService.issueAccessToken(new Principal("user-rotated"));

		assertThat(parseClaims(token, newKey).getSubject()).isEqualTo("user-rotated");
		assertThat(Jwts.parserBuilder().setSigningKey(newKey).build().parseClaimsJws(token).getHeader().get(JwsHeader.KEY_ID))
			.isEqualTo("new");
		assertThat(rotatingService.verifyAccessToken(token).getUserId()).isEqualTo("user-rotated");
	}

	@Test
	void rejectsRevokedAccessTokenByJti() {
		InMemoryTokenRevocationStore revocationStore = new InMemoryTokenRevocationStore();
		JwtTokenService service = new JwtTokenService(SECRET, 60, 120, revocationStore);
		String token = service.issueAccessToken(new Principal("user-1"));

		service.revokeAccessToken(token);

		assertThatThrownBy(() -> service.verifyAccessToken(token))
			.isInstanceOf(AuthException.class)
			.extracting(error -> ((AuthException) error).getReason())
			.isEqualTo(AuthFailureReason.REVOKED_TOKEN);
	}

	@Test
	void supportsRsaConstructor() throws Exception {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(2048);
		KeyPair keyPair = generator.generateKeyPair();

		JwtTokenService service = new JwtTokenService(keyPair.getPrivate(), keyPair.getPublic(), 60, 120);
		String token = service.issueAccessToken(new Principal("rsa-user"));

		assertThat(service.verifyAccessToken(token).getUserId()).isEqualTo("rsa-user");
	}

	@Test
	void supportsJwksResolverWithRsaKey() throws Exception {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(2048);
		KeyPair keyPair = generator.generateKeyPair();
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		String modulus = Encoders.BASE64URL.encode(trimmedUnsignedBytes(publicKey.getModulus().toByteArray()));
		String exponent = Encoders.BASE64URL.encode(trimmedUnsignedBytes(publicKey.getPublicExponent().toByteArray()));
		String jwksJson = "{\"keys\":[{\"kty\":\"RSA\",\"kid\":\"rsa-1\",\"n\":\"" + modulus + "\",\"e\":\"" + exponent + "\"}]}";

		JwtTokenService service = new JwtTokenService(
			new StaticJwtSigningKeyProvider(keyPair.getPrivate(), "rsa-1"),
			new JwksJwtKeyResolver(jwksJson),
			new DefaultJwtClaimsMapper(),
			new JwtTokenOptions(Duration.ofMinutes(1), Duration.ofMinutes(2), SignatureAlgorithm.RS256, null)
		);

		String token = service.issueAccessToken(new Principal("rsa-user"));

		assertThat(service.verifyAccessToken(token).getUserId()).isEqualTo("rsa-user");
	}

	private Claims parseClaims(String token) {
		Key key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
		return parseClaims(token, key);
	}

	private Claims parseClaims(String token, Key key) {
		return Jwts.parserBuilder()
			.setSigningKey(key)
			.build()
			.parseClaimsJws(token)
			.getBody();
	}

	private static byte[] trimmedUnsignedBytes(byte[] source) {
		if (source.length > 1 && source[0] == 0) {
			byte[] trimmed = new byte[source.length - 1];
			System.arraycopy(source, 1, trimmed, 0, trimmed.length);
			return trimmed;
		}
		return source;
	}

	private static final class InMemoryTokenRevocationStore implements TokenRevocationStore {
		private final Map<String, Instant> revoked = new ConcurrentHashMap<>();

		@Override
		public void revoke(String tokenId, Instant expiresAt) {
			revoked.put(tokenId, expiresAt);
		}

		@Override
		public boolean isRevoked(String tokenId) {
			return revoked.containsKey(tokenId);
		}
	}
}
