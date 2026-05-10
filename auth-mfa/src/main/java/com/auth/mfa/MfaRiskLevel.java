package com.auth.mfa;

/** 상위 정책 계층이 전달하는 정규화된 위험도 수준입니다. */
public enum MfaRiskLevel {
	UNKNOWN,
	LOW,
	MEDIUM,
	HIGH,
	CRITICAL;

	public boolean meetsOrExceeds(MfaRiskLevel threshold) {
		if (threshold == null) return false;
		if (this == UNKNOWN) return false;
		if (threshold == UNKNOWN) return true;
		return ordinal() >= threshold.ordinal();
	}
}
