package com.auth.dpop;

import java.time.Instant;

/** 허용된 DPoP 시간 창 안에서 재사용을 막기 위해 proof 식별자를 잠시 저장합니다. */
public interface DpopReplayValidator {

	boolean markIfNew(String tokenId, Instant expiresAt);
}
