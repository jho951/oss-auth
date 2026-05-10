package com.auth.session.id;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

/** 세션 ID 생성 */
public final class SecureRandomSessionIdGenerator implements SessionIdGenerator {

    private static final int DEFAULT_BYTES = 24;
    private final SecureRandom random;
    private final int byteLength;

    public SecureRandomSessionIdGenerator() {
        this(DEFAULT_BYTES);
    }

    public SecureRandomSessionIdGenerator(int byteLength) {
        this(byteLength, new SecureRandom());
    }

    SecureRandomSessionIdGenerator(int byteLength, SecureRandom random) {
        if (byteLength <= 0) {
            throw new IllegalArgumentException("byteLength must be positive");
        }
        this.byteLength = byteLength;
        this.random = Objects.requireNonNull(random, "random");
    }

    @Override
    public String generate() {
        byte[] bytes = new byte[byteLength];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
