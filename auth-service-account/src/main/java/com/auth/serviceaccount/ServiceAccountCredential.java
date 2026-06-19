package com.auth.serviceaccount;

/** 범용 서비스 계정 자격 증명입니다. */
public final class ServiceAccountCredential {

	private final String serviceId;
	private final String secret;

	public ServiceAccountCredential(String serviceId, String secret) {
		this.serviceId = serviceId;
		this.secret = secret;
	}

	public String serviceId() {
		return serviceId;
	}

	public String secret() {
		return secret;
	}
}
