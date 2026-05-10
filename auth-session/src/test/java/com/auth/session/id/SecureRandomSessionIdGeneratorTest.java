package com.auth.session.id;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SecureRandomSessionIdGeneratorTest {

    @Test
    @DisplayName("기본 생성자는 32글자 URL-safe session id를 만든다")
    void defaultConstructorGeneratesThirtyTwoCharacterSessionId() {
        SecureRandomSessionIdGenerator generator = new SecureRandomSessionIdGenerator();

        String sessionId = generator.generate();

        assertThat(sessionId).hasSize(32);
        assertThat(sessionId).doesNotContain("+", "/", "=");
    }

    @Test
    @DisplayName("바이트 길이를 지정하면 인코딩 길이도 함께 달라진다")
    void configurableByteLengthChangesEncodedLength() {
        SecureRandomSessionIdGenerator generator = new SecureRandomSessionIdGenerator(16);

        String sessionId = generator.generate();

        assertThat(sessionId).hasSize(22);
        assertThat(sessionId).doesNotContain("+", "/", "=");
    }

    @Test
    @DisplayName("바이트 길이는 1 이상이어야 한다")
    void rejectsNonPositiveByteLength() {
        assertThatThrownBy(() -> new SecureRandomSessionIdGenerator(0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("byteLength must be positive");
    }
}
