package com.auth.dpop;

import java.time.Instant;
import java.util.Map;

/** 상위 정책 계층에서 사용할 수 있도록 정규화된 DPoP proof 검증 결과입니다. */
public final class DpopProof {

	private final String tokenId;
	private final String method;
	private final String uri;
	private final Instant issuedAt;
	private final String jwkThumbprint;
	private final Map<String, Object> claims;

	public DpopProof(String tokenId, String method, String uri, Instant issuedAt, String jwkThumbprint, Map<String, Object> claims) {
		this.tokenId = tokenId;
		this.method = method;
		this.uri = uri;
		this.issuedAt = issuedAt;
		this.jwkThumbprint = jwkThumbprint;
		this.claims = claims == null ? Map.of() : Map.copyOf(claims);
	}

	public String getTokenId() {
		return tokenId;
	}

	public String getMethod() {
		return method;
	}

	public String getUri() {
		return uri;
	}

	public Instant getIssuedAt() {
		return issuedAt;
	}

	public String getJwkThumbprint() {
		return jwkThumbprint;
	}

	public Map<String, Object> getClaims() {
		return claims;
	}
}
