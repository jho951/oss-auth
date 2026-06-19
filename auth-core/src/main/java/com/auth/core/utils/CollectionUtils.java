package com.auth.core.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Java 8 compatible immutable collection factories used across modules. */
public final class CollectionUtils {

	private CollectionUtils() {
	}

	public static <T> List<T> listOf() {
		return Collections.emptyList();
	}

	@SafeVarargs
	public static <T> List<T> listOf(T... values) {
		if (values == null || values.length == 0) {
			return Collections.emptyList();
		}
		ArrayList<T> list = new ArrayList<>(values.length);
		Collections.addAll(list, values);
		return Collections.unmodifiableList(list);
	}

	public static <T> List<T> copyList(Iterable<? extends T> values) {
		if (values == null) {
			return Collections.emptyList();
		}
		ArrayList<T> list = new ArrayList<>();
		for (T value : values) {
			list.add(value);
		}
		return Collections.unmodifiableList(list);
	}

	public static <K, V> Map<K, V> mapOf() {
		return Collections.emptyMap();
	}

	public static <K, V> Map<K, V> mapOf(K k1, V v1) {
		LinkedHashMap<K, V> map = new LinkedHashMap<>();
		map.put(k1, v1);
		return Collections.unmodifiableMap(map);
	}

	public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2) {
		LinkedHashMap<K, V> map = new LinkedHashMap<>();
		map.put(k1, v1);
		map.put(k2, v2);
		return Collections.unmodifiableMap(map);
	}

	public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3) {
		LinkedHashMap<K, V> map = new LinkedHashMap<>();
		map.put(k1, v1);
		map.put(k2, v2);
		map.put(k3, v3);
		return Collections.unmodifiableMap(map);
	}

	public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
		LinkedHashMap<K, V> map = new LinkedHashMap<>();
		map.put(k1, v1);
		map.put(k2, v2);
		map.put(k3, v3);
		map.put(k4, v4);
		return Collections.unmodifiableMap(map);
	}

	public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
		LinkedHashMap<K, V> map = new LinkedHashMap<>();
		map.put(k1, v1);
		map.put(k2, v2);
		map.put(k3, v3);
		map.put(k4, v4);
		map.put(k5, v5);
		return Collections.unmodifiableMap(map);
	}

	public static <K, V> Map<K, V> copyMap(Map<? extends K, ? extends V> values) {
		if (values == null || values.isEmpty()) {
			return Collections.emptyMap();
		}
		return Collections.unmodifiableMap(new LinkedHashMap<K, V>(values));
	}

	public static <T> Set<T> setOf() {
		return Collections.emptySet();
	}

	@SafeVarargs
	public static <T> Set<T> setOf(T... values) {
		if (values == null || values.length == 0) {
			return Collections.emptySet();
		}
		LinkedHashSet<T> set = new LinkedHashSet<>();
		Collections.addAll(set, values);
		return Collections.unmodifiableSet(set);
	}

	public static <T> Set<T> copySet(Iterable<? extends T> values) {
		if (values == null) {
			return Collections.emptySet();
		}
		LinkedHashSet<T> set = new LinkedHashSet<>();
		for (T value : values) {
			set.add(value);
		}
		return Collections.unmodifiableSet(set);
	}
}
