package com.auth.dpop;

import com.auth.core.utils.Strings;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SigningKeyResolverAdapter;
import java.security.Key;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** DPoP proof JWT를 검증하고 method, URI, 시간, nonce, ath, replay 제약을 강제합니다. */
public final class DpopProofVerifier {

	private final Clock clock;
	private final Duration maxProofAge;
	private final DpopReplayValidator replayValidator;

	public DpopProofVerifier() {
		this(Clock.systemUTC(), Duration.ofMinutes(5), new InMemoryDpopReplayValidator());
	}

	public DpopProofVerifier(Clock clock, Duration maxProofAge, DpopReplayValidator replayValidator) {
		this.clock = Objects.requireNonNull(clock, "clock");
		if (maxProofAge == null || maxProofAge.isZero() || maxProofAge.isNegative()) {
			throw new IllegalArgumentException("maxProofAge must be positive");
		}
		this.maxProofAge = maxProofAge;
		this.replayValidator = Objects.requireNonNull(replayValidator, "replayValidator");
	}

	public Optional<DpopProof> verify(DpopProofRequest request) {
		if (request == null
			|| Strings.isBlank(request.getProofJwt())
			|| Strings.isBlank(request.getMethod())
			|| Strings.isBlank(request.getUri())) {
			return Optional.empty();
		}

		try {
			Map<String, Object>[] jwkHolder = new Map[1];
			Jws<Claims> jws = Jwts.parserBuilder()
				.setSigningKeyResolver(new SigningKeyResolverAdapter() {
					@Override
					public Key resolveSigningKey(JwsHeader header, Claims claims) {
						@SuppressWarnings("unchecked")
						Map<String, Object> jwk = (Map<String, Object>) header.get("jwk");
						jwkHolder[0] = jwk;
						return DpopTokenBindingHelper.toPublicKey(jwk);
					}
				})
				.build()
				.parseClaimsJws(request.getProofJwt());

			JwsHeader header = jws.getHeader();
			Claims claims = jws.getBody();
			if (header == null || !"dpop+jwt".equalsIgnoreCase(header.getType())) return Optional.empty();
			if (jwkHolder[0] == null || jwkHolder[0].isEmpty()) return Optional.empty();

			String method = stringClaim(claims, "htm");
			String normalizedUri = DpopTokenBindingHelper.normalizeUri(stringClaim(claims, "htu"));
			String normalizedRequestUri = DpopTokenBindingHelper.normalizeUri(request.getUri());
			if (!request.getMethod().equalsIgnoreCase(method)) return Optional.empty();
			if (!normalizedRequestUri.equals(normalizedUri)) return Optional.empty();

			if (!Strings.isBlank(request.getExpectedNonce())) {
				String nonce = stringClaim(claims, "nonce");
				if (!request.getExpectedNonce().equals(nonce)) return Optional.empty();
			}

			if (!Strings.isBlank(request.getAccessToken())) {
				String ath = stringClaim(claims, "ath");
				if (!DpopTokenBindingHelper.accessTokenHash(request.getAccessToken()).equals(ath)) return Optional.empty();
			}

			Date issuedAt = claims.getIssuedAt();
			String tokenId = claims.getId();
			if (issuedAt == null || Strings.isBlank(tokenId)) return Optional.empty();
			Instant now = Instant.now(clock);
			Instant proofIssuedAt = issuedAt.toInstant();
			if (proofIssuedAt.isAfter(now.plusSeconds(5))) return Optional.empty();
			if (proofIssuedAt.isBefore(now.minus(maxProofAge))) return Optional.empty();
			if (!replayValidator.markIfNew(tokenId, proofIssuedAt.plus(maxProofAge))) return Optional.empty();

			Map<String, Object> attributes = new HashMap<>(claims);
			attributes.put("jwk", com.auth.core.utils.CollectionUtils.copyMap(jwkHolder[0]));
			return Optional.of(new DpopProof(
				tokenId,
				method.toUpperCase(),
				normalizedUri,
				proofIssuedAt,
				DpopTokenBindingHelper.jwkThumbprint(jwkHolder[0]),
				attributes
			));
		} catch (RuntimeException e) {
			return Optional.empty();
		}
	}

	private static String stringClaim(Claims claims, String name) {
		String value = claims.get(name, String.class);
		if (Strings.isBlank(value)) throw new IllegalArgumentException("missing claim: " + name);
		return value;
	}
}
