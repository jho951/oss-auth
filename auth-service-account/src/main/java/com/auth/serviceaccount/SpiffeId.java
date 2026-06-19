package com.auth.serviceaccount;

import com.auth.core.utils.Strings;
import java.util.ArrayList;
import java.net.URI;
import java.util.List;

/** URI SAN 값에서 추출한 최소 SPIFFE 식별자 모델입니다. */
public final class SpiffeId {

	private final String trustDomain;
	private final String workloadPath;

	private SpiffeId(String trustDomain, String workloadPath) {
		if (Strings.isBlank(trustDomain)) throw new IllegalArgumentException("trustDomain must not be blank");
		this.trustDomain = trustDomain;
		this.workloadPath = workloadPath == null ? "/" : workloadPath;
	}

	public static SpiffeId parse(String value) {
		if (Strings.isBlank(value)) throw new IllegalArgumentException("SPIFFE ID must not be blank");
		URI uri = URI.create(value);
		if (!"spiffe".equalsIgnoreCase(uri.getScheme())) throw new IllegalArgumentException("SPIFFE ID must use spiffe scheme");
		if (Strings.isBlank(uri.getHost())) throw new IllegalArgumentException("SPIFFE trust domain must not be blank");
		return new SpiffeId(uri.getHost(), normalizePath(uri.getPath()));
	}

	public String getTrustDomain() {
		return trustDomain;
	}

	public String getWorkloadPath() {
		return workloadPath;
	}

	public List<String> getPathSegments() {
		ArrayList<String> segments = new ArrayList<>();
		for (String segment : workloadPath.split("/")) {
			if (!Strings.isBlank(segment)) {
				segments.add(segment);
			}
		}
		return com.auth.core.utils.CollectionUtils.copyList(segments);
	}

	@Override
	public String toString() {
		return "spiffe://" + trustDomain + workloadPath;
	}

	private static String normalizePath(String path) {
		if (Strings.isBlank(path)) return "/";
		return path.startsWith("/") ? path : "/" + path;
	}
}
