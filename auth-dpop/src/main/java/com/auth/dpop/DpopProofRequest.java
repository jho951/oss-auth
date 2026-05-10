package com.auth.dpop;

/** 요청에서 추출한 정규화 DPoP proof 입력입니다. */
public final class DpopProofRequest {

	private final String proofJwt;
	private final String method;
	private final String uri;
	private final String accessToken;
	private final String expectedNonce;

	public DpopProofRequest(String proofJwt, String method, String uri, String accessToken, String expectedNonce) {
		this.proofJwt = proofJwt == null ? "" : proofJwt.trim();
		this.method = method == null ? "" : method.trim();
		this.uri = uri == null ? "" : uri.trim();
		this.accessToken = accessToken == null ? "" : accessToken;
		this.expectedNonce = expectedNonce == null ? "" : expectedNonce.trim();
	}

	public String getProofJwt() {
		return proofJwt;
	}

	public String getMethod() {
		return method;
	}

	public String getUri() {
		return uri;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public String getExpectedNonce() {
		return expectedNonce;
	}
}
