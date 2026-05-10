package com.auth.common.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.auth.core.utils.Strings;

class StringsTest {

	@Test
	void requireNonBlank_ReturnsValue_WhenValid() {
		assertThat(Strings.requireNonBlank("value", "field")).isEqualTo("value");
	}

	@Test
	void requireNonBlank_Throws_WhenBlank() {
		assertThatThrownBy(() -> Strings.requireNonBlank(" ", "field"))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("field must not be blank");
	}

	@Test
	void requireNonNull_ReturnsValue_WhenValid() {
		assertThat(Strings.requireNonNull(123, "num")).isEqualTo(123);
	}

	@Test
	void requireNonNull_Throws_WhenNull() {
		assertThatThrownBy(() -> Strings.requireNonNull(null, "num"))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("num must not be null");
	}
}
