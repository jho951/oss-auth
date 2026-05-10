package com.auth.mfa;

import java.util.Locale;

/** 지원하는 2차 인증 수단 분류입니다. */
public enum MfaFactorType {
	TOTP,
	OTP,
	PASSKEY,
	RECOVERY_CODE,
	PUSH,
	CUSTOM;

	public String attributeValue() {
		return name().toLowerCase(Locale.ROOT);
	}
}
