package com.auth.mfa;

import com.auth.core.api.model.Principal;
import com.auth.core.utils.Strings;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** principal에 MFA 결과 속성을 기록하는 기본 매퍼입니다. */
public final class DefaultMfaPrincipalMapper implements MfaPrincipalMapper {

	private final Clock clock;

	public DefaultMfaPrincipalMapper() {
		this(Clock.systemUTC());
	}

	public DefaultMfaPrincipalMapper(Clock clock) {
		this.clock = Objects.requireNonNull(clock, "clock");
	}

	@Override
	public Principal map(
		Principal principal,
		MfaEnrollment enrollment,
		Map<String, Object> verificationAttributes,
		MfaChallengeContext context
	) {
		Objects.requireNonNull(principal, "principal");
		Objects.requireNonNull(enrollment, "enrollment");

		Map<String, Object> attributes = new LinkedHashMap<>(principal.getAttributes());
		if (verificationAttributes != null && !verificationAttributes.isEmpty()) {
			attributes.putAll(verificationAttributes);
		}
		attributes.put("mfa_authenticated", true);
		attributes.put("mfa_factor_id", enrollment.getFactorId());
		attributes.put("mfa_factor_type", enrollment.getFactorType().attributeValue());
		attributes.put("mfa_authenticated_at", Instant.now(clock));
		if (context != null) {
			context.getActionIfPresent().ifPresent(action -> attributes.put("mfa_action", action));
			attributes.put("mfa_risk_level", context.getRiskLevel().name());
		}
		attributes.put("amr", appendAmr(attributes.get("amr"), enrollment.getFactorType().attributeValue()));

		return new Principal(principal.getUserId(), principal.getAuthorities(), attributes);
	}

	private static List<String> appendAmr(Object existing, String factorValue) {
		ArrayList<String> values = new ArrayList<>();
		if (existing instanceof String && !Strings.isBlank((String) existing)) {
			String stringValue = (String) existing;
			values.add(stringValue);
		} else if (existing instanceof Iterable<?>) {
			Iterable<?> iterable = (Iterable<?>) existing;
			for (Object item : iterable) {
				if (item != null) {
					String value = item.toString();
					if (!Strings.isBlank(value) && !values.contains(value)) {
						values.add(value);
					}
				}
			}
		}
		if (!values.contains(factorValue)) {
			values.add(factorValue);
		}
		return com.auth.core.utils.CollectionUtils.copyList(values);
	}
}
