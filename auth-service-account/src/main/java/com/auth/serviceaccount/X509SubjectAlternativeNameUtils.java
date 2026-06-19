package com.auth.serviceaccount;

import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** workload identity에서 자주 사용하는 subject alternative name을 추출합니다. */
public final class X509SubjectAlternativeNameUtils {

	private static final Integer URI_SAN_TYPE = 6;

	private X509SubjectAlternativeNameUtils() {
	}

	public static List<URI> uriSubjectAlternativeNames(X509Certificate certificate) {
		if (certificate == null) return com.auth.core.utils.CollectionUtils.listOf();
		try {
			Collection<List<?>> subjectAlternativeNames = certificate.getSubjectAlternativeNames();
			if (subjectAlternativeNames == null) return com.auth.core.utils.CollectionUtils.listOf();
			ArrayList<URI> uris = new ArrayList<>();
			for (List<?> entry : subjectAlternativeNames) {
				if (entry == null || entry.size() < 2) continue;
				if (!URI_SAN_TYPE.equals(entry.get(0))) continue;
				Object value = entry.get(1);
				if (value != null) {
					uris.add(URI.create(String.valueOf(value)));
				}
			}
			return com.auth.core.utils.CollectionUtils.copyList(uris);
		} catch (Exception e) {
			throw new IllegalArgumentException("failed to read subject alternative names", e);
		}
	}
}
