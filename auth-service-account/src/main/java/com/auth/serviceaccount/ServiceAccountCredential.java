package com.auth.serviceaccount;

/** 범용 서비스 계정 자격 증명입니다. */
public record ServiceAccountCredential(String serviceId, String secret) {
}
