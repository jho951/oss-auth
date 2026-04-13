package com.auth.api.authentication;

/** Generic source/capability that produced an authentication result. */
public enum AuthenticationSource {
	JWT,
	SESSION,
	API_KEY,
	HMAC,
	MTLS,
	OIDC,
	SERVICE_ACCOUNT,
	ONE_TIME_TOKEN,
	UNKNOWN
}
