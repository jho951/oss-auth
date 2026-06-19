package com.auth.core.utils;

public final class Strings {
	private Strings() {}

	public static boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}

	public static String requireNonBlank(String value, String name) {
		if (isBlank(value)) throw new IllegalArgumentException(name + " must not be blank");
		return value;
	}

	public static <T> T requireNonNull(T value, String name) {
		if (value == null) throw new IllegalArgumentException(name + " must not be null");
		return value;
	}

	public static String toStringOrNull(Object value) {
		return value == null ? null : String.valueOf(value);
	}
}
