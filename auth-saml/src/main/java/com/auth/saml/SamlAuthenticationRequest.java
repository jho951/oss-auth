package com.auth.saml;

import java.util.Map;

/** servlet이나 XML 전송 세부사항과 분리된 정규화 SAML 인증 입력입니다. */
public final class SamlAuthenticationRequest {

	private final String responseXml;
	private final String expectedAudience;
	private final String expectedRecipient;
	private final String requestId;
	private final Map<String, Object> attributes;

	public SamlAuthenticationRequest(
		String responseXml,
		String expectedAudience,
		String expectedRecipient,
		String requestId,
		Map<String, Object> attributes
	) {
		this.responseXml = responseXml == null ? "" : responseXml;
		this.expectedAudience = expectedAudience == null ? "" : expectedAudience.trim();
		this.expectedRecipient = expectedRecipient == null ? "" : expectedRecipient.trim();
		this.requestId = requestId == null ? "" : requestId.trim();
		this.attributes = attributes == null ? com.auth.core.utils.CollectionUtils.mapOf() : com.auth.core.utils.CollectionUtils.copyMap(attributes);
	}

	public String getResponseXml() {
		return responseXml;
	}

	public String getExpectedAudience() {
		return expectedAudience;
	}

	public String getExpectedRecipient() {
		return expectedRecipient;
	}

	public String getRequestId() {
		return requestId;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}
}
