package com.auth.saml;

import com.auth.core.utils.Strings;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/** principal 매핑과 정책 계층이 공유하는 정규화된 SAML assertion 필드입니다. */
public final class SamlAssertion {

	private final String subject;
	private final String issuer;
	private final List<String> audiences;
	private final String recipient;
	private final String inResponseTo;
	private final String sessionIndex;
	private final Instant issuedAt;
	private final Instant notBefore;
	private final Instant notOnOrAfter;
	private final Map<String, Object> attributes;

	public SamlAssertion(
		String subject,
		String issuer,
		List<String> audiences,
		String recipient,
		String inResponseTo,
		String sessionIndex,
		Instant issuedAt,
		Instant notBefore,
		Instant notOnOrAfter,
		Map<String, Object> attributes
	) {
		if (Strings.isBlank(subject)) throw new IllegalArgumentException("subject must not be blank");
		if (Strings.isBlank(issuer)) throw new IllegalArgumentException("issuer must not be blank");
		this.subject = subject;
		this.issuer = issuer;
		this.audiences = audiences == null ? com.auth.core.utils.CollectionUtils.listOf() : com.auth.core.utils.CollectionUtils.copyList(audiences);
		this.recipient = recipient == null ? "" : recipient;
		this.inResponseTo = inResponseTo == null ? "" : inResponseTo;
		this.sessionIndex = sessionIndex == null ? "" : sessionIndex;
		this.issuedAt = issuedAt;
		this.notBefore = notBefore;
		this.notOnOrAfter = notOnOrAfter;
		this.attributes = attributes == null ? com.auth.core.utils.CollectionUtils.mapOf() : com.auth.core.utils.CollectionUtils.copyMap(attributes);
	}

	public String getSubject() {
		return subject;
	}

	public String getIssuer() {
		return issuer;
	}

	public List<String> getAudiences() {
		return audiences;
	}

	public String getRecipient() {
		return recipient;
	}

	public String getInResponseTo() {
		return inResponseTo;
	}

	public String getSessionIndex() {
		return sessionIndex;
	}

	public Instant getIssuedAt() {
		return issuedAt;
	}

	public Instant getNotBefore() {
		return notBefore;
	}

	public Instant getNotOnOrAfter() {
		return notOnOrAfter;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}
}
