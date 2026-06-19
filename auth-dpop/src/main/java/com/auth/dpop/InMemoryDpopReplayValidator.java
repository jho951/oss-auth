package com.auth.dpop;

import com.auth.core.utils.Strings;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/** 테스트나 단일 노드 배포에 적합한 메모리 기반 replay validator입니다. */
public final class InMemoryDpopReplayValidator implements DpopReplayValidator {

	private final Clock clock;
	private final Map<String, Instant> seenProofs = new ConcurrentHashMap<>();

	public InMemoryDpopReplayValidator() {
		this(Clock.systemUTC());
	}

	public InMemoryDpopReplayValidator(Clock clock) {
		this.clock = Objects.requireNonNull(clock, "clock");
	}

	@Override
	public boolean markIfNew(String tokenId, Instant expiresAt) {
		if (Strings.isBlank(tokenId)) return false;
		Instant now = Instant.now(clock);
		seenProofs.entrySet().removeIf(entry -> !entry.getValue().isAfter(now));
		Instant existing = seenProofs.putIfAbsent(tokenId, expiresAt == null ? now : expiresAt);
		return existing == null;
	}
}
