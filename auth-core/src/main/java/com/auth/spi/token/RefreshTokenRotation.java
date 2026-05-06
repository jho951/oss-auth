package com.auth.spi.token;

import java.time.Instant;
import java.util.Objects;

/** 리프레시 토큰 갱신 */
public final class RefreshTokenRotation {
	/** 이제 막 사용되어 폐기될(이전) 리프레시 토큰 */
	private final String previousToken;
	/** 새로 발급되어 앞으로 사용될 신규 리프레시 토큰 */
	private final String nextToken;
	/** 새 토큰이 언제까지 유효한지 나타내는 만료 시각 */
	private final Instant nextExpiresAt;

	public RefreshTokenRotation(String previousToken, String nextToken, Instant nextExpiresAt) {
		this.previousToken = previousToken;
		this.nextToken = Objects.requireNonNull(nextToken, "nextToken");
		this.nextExpiresAt = Objects.requireNonNull(nextExpiresAt, "nextExpiresAt");
	}

	public String getPreviousToken() {
		return previousToken;
	}
	public String getNextToken() {
		return nextToken;
	}
	public Instant getNextExpiresAt() {
		return nextExpiresAt;
	}
}
