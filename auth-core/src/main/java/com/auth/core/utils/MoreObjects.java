package com.auth.core.utils;

/** null 처리와 기본값 선택에 사용하는 공통 유틸입니다. */
public final class MoreObjects {
	private MoreObjects() {}

	public static <T> T defaultIfNull(T value, T defaultValue) {
		return value == null ? defaultValue : value;
	}
}
