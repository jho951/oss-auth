package com.auth.api.model;

/** Generic principal categories independent of service-specific roles. */
public enum PrincipalType {
	USER,
	SERVICE,
	ANONYMOUS,
	DELEGATED,
	IMPERSONATION,
	UNKNOWN
}
