package com.auth.serviceaccount;

import java.net.URI;
import java.util.List;

/** URI SAN 값에서 추출한 최소 SPIFFE 식별자 모델입니다. */
public final class SpiffeId {

	private final String trustDomain;
	private final String workloadPath;

	private SpiffeId(String trustDomain, String workloadPath) {
		if (trustDomain == null || trustDomain.isBlank()) throw new IllegalArgumentException("trustDomain must not be blank");
		this.trustDomain = trustDomain;
		this.workloadPath = workloadPath == null ? "/" : workloadPath;
	}

	public static SpiffeId parse(String value) {
		if (value == null || value.isBlank()) throw new IllegalArgumentException("SPIFFE ID must not be blank");
		URI uri = URI.create(value);
		if (!"spiffe".equalsIgnoreCase(uri.getScheme())) throw new IllegalArgumentException("SPIFFE ID must use spiffe scheme");
		if (uri.getHost() == null || uri.getHost().isBlank()) throw new IllegalArgumentException("SPIFFE trust domain must not be blank");
		return new SpiffeId(uri.getHost(), normalizePath(uri.getPath()));
	}

	public String getTrustDomain() {
		return trustDomain;
	}

	public String getWorkloadPath() {
		return workloadPath;
	}

	public List<String> getPathSegments() {
		return List.of(workloadPath.split("/")).stream().filter(segment -> !segment.isBlank()).toList();
	}

	@Override
	public String toString() {
		return "spiffe://" + trustDomain + workloadPath;
	}

	private static String normalizePath(String path) {
		if (path == null || path.isBlank()) return "/";
		return path.startsWith("/") ? path : "/" + path;
	}
}
