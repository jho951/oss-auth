package com.auth.session;

import com.auth.api.model.Principal;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/** Stored session value with generic metadata for expiration, device, and concurrency strategies. */
public final class SessionRecord {

	private final String sessionId;
	private final Principal principal;
	private final Instant createdAt;
	private final Instant lastAccessedAt;
	private final Instant expiresAt;
	private final Map<String, Object> attributes;

	public SessionRecord(String sessionId, Principal principal, Instant createdAt, Instant lastAccessedAt, Instant expiresAt, Map<String, Object> attributes) {
		this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
		this.principal = Objects.requireNonNull(principal, "principal");
		this.createdAt = createdAt == null ? Instant.now() : createdAt;
		this.lastAccessedAt = lastAccessedAt == null ? this.createdAt : lastAccessedAt;
		this.expiresAt = expiresAt;
		this.attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
	}

	public static SessionRecord active(String sessionId, Principal principal) {
		return new SessionRecord(sessionId, principal, Instant.now(), Instant.now(), null, Map.of());
	}

	public String getSessionId() {
		return sessionId;
	}

	public Principal getPrincipal() {
		return principal;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getLastAccessedAt() {
		return lastAccessedAt;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public boolean isExpired(Instant now) {
		return expiresAt != null && !expiresAt.isAfter(now == null ? Instant.now() : now);
	}

	public SessionRecord accessedAt(Instant now, Instant expiresAt) {
		return new SessionRecord(sessionId, principal, createdAt, now == null ? Instant.now() : now, expiresAt, attributes);
	}
}
