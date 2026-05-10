package com.auth.otp;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/** 시간 간격과 허용 오차를 조절할 수 있는 RFC 6238 기반 TOTP 검증기입니다. */
public final class TotpVerifier {

	private final HotpVerifier hotpVerifier;
	private final Clock clock;
	private final Duration timeStep;
	private final int allowedSkewWindows;

	public TotpVerifier() {
		this(Duration.ofSeconds(30), 1, 6, OtpHashAlgorithm.SHA1, Clock.systemUTC());
	}

	public TotpVerifier(Duration timeStep, int allowedSkewWindows, int digits, OtpHashAlgorithm algorithm, Clock clock) {
		if (timeStep == null || timeStep.isZero() || timeStep.isNegative()) {
			throw new IllegalArgumentException("timeStep must be positive");
		}
		if (allowedSkewWindows < 0) throw new IllegalArgumentException("allowedSkewWindows must not be negative");
		this.timeStep = timeStep;
		this.allowedSkewWindows = allowedSkewWindows;
		this.hotpVerifier = new HotpVerifier(digits, algorithm);
		this.clock = Objects.requireNonNull(clock, "clock");
	}

	public boolean verify(String base32Secret, String code) {
		return verify(base32Secret, Instant.now(clock), code);
	}

	public boolean verify(String base32Secret, Instant timestamp, String code) {
		long counter = counterFor(timestamp);
		for (int delta = -allowedSkewWindows; delta <= allowedSkewWindows; delta++) {
			if (hotpVerifier.verify(base32Secret, counter + delta, code)) {
				return true;
			}
		}
		return false;
	}

	public String generate(String base32Secret, Instant timestamp) {
		return hotpVerifier.generate(base32Secret, counterFor(timestamp));
	}

	private long counterFor(Instant timestamp) {
		return timestamp.getEpochSecond() / timeStep.getSeconds();
	}
}
