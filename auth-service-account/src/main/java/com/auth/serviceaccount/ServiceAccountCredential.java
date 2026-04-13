package com.auth.serviceaccount;

/** Generic service account credential. */
public record ServiceAccountCredential(String serviceId, String secret) {
}
