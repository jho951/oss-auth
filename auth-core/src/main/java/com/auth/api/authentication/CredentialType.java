package com.auth.api.authentication;

/** Credential category understood by generic authentication orchestration. */
public enum CredentialType {
	BEARER_TOKEN,
	REFRESH_TOKEN,
	SESSION_ID,
	API_KEY,
	HMAC_SIGNATURE,
	CLIENT_CERTIFICATE,
	OIDC_ID_TOKEN,
	SERVICE_ACCOUNT_SECRET,
	ONE_TIME_TOKEN
}
