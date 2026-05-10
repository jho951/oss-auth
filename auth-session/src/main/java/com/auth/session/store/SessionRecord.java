package com.auth.session.store;

import com.auth.core.api.model.Principal;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/** 세션의 상태와 메타데이터를 포함 모델 */
public final class SessionRecord {

    /** 세션의 고유 식별자 (저장소에서 조회 키) */
	private final String sessionId;
    /** 인증된 사용자의 신원 정보 */
	private final Principal principal;
    /** 세션이 최초로 생성된(로그인) 시점 */
	private final Instant createdAt;
    /** 세션 타임아웃(Idle Timeout) 판단의 근거가 되는 마지막 활동 시점 */
	private final Instant lastAccessedAt;
    /** 세션이 절대적으로 만료되는 시점(Absolute Timeout) */
    private final Instant expiresAt;
    /** 부가적인 컨텍스트를 저장 */
	private final Map<String, Object> attributes;

	public SessionRecord(String sessionId, Principal principal, Instant createdAt, Instant lastAccessedAt, Instant expiresAt, Map<String, Object> attributes) {
		this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
		this.principal = Objects.requireNonNull(principal, "principal");
		this.createdAt = createdAt == null ? Instant.now() : createdAt;
		this.lastAccessedAt = lastAccessedAt == null ? this.createdAt : lastAccessedAt;
		this.expiresAt = expiresAt;
		this.attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
	}

	public String getSessionId() {return sessionId;}
	public Principal getPrincipal() {return principal;}
	public Instant getCreatedAt() {return createdAt;}
	public Instant getLastAccessedAt() {return lastAccessedAt;}
	public Instant getExpiresAt() {return expiresAt;}
	public Map<String, Object> getAttributes() {return attributes;}

    public static SessionRecord active(String sessionId, Principal principal) {
        return new SessionRecord(sessionId, principal, Instant.now(), Instant.now(), null, Map.of());
    }

    public SessionRecord accessedAt(Instant now, Instant expiresAt) {
        return new SessionRecord(sessionId, principal, createdAt, now == null ? Instant.now() : now, expiresAt, attributes);
    }

    public boolean isExpired(Instant now) {
		return expiresAt != null && !expiresAt.isAfter(now == null ? Instant.now() : now);
	}
}
