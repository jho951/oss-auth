package com.auth.webauthn;

import java.util.Arrays;

/** 인증기에서 반환한 authenticator data의 핵심 필드를 담는 값 객체입니다. */
public final class WebAuthnAuthenticatorData {

	private final byte[] rawBytes;
	private final byte[] rpIdHash;
	private final boolean userPresent;
	private final boolean userVerified;
	private final long signCount;

	public WebAuthnAuthenticatorData(
		byte[] rawBytes,
		byte[] rpIdHash,
		boolean userPresent,
		boolean userVerified,
		long signCount
	) {
		this.rawBytes = rawBytes == null ? new byte[0] : rawBytes.clone();
		this.rpIdHash = rpIdHash == null ? new byte[0] : rpIdHash.clone();
		this.userPresent = userPresent;
		this.userVerified = userVerified;
		this.signCount = signCount;
	}

	public byte[] getRawBytes() {
		return rawBytes.clone();
	}

	public byte[] getRpIdHash() {
		return rpIdHash.clone();
	}

	public boolean isUserPresent() {
		return userPresent;
	}

	public boolean isUserVerified() {
		return userVerified;
	}

	public long getSignCount() {
		return signCount;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof WebAuthnAuthenticatorData that)) return false;
		return userPresent == that.userPresent
			&& userVerified == that.userVerified
			&& signCount == that.signCount
			&& Arrays.equals(rawBytes, that.rawBytes)
			&& Arrays.equals(rpIdHash, that.rpIdHash);
	}

	@Override
	public int hashCode() {
		int result = Arrays.hashCode(rawBytes);
		result = 31 * result + Arrays.hashCode(rpIdHash);
		result = 31 * result + Boolean.hashCode(userPresent);
		result = 31 * result + Boolean.hashCode(userVerified);
		result = 31 * result + Long.hashCode(signCount);
		return result;
	}
}
