package com.auth.mfa;

import com.auth.otp.TotpVerifier;
import com.auth.core.utils.Strings;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** TOTP 기반 MFA 증명을 검증하는 기본 구현입니다. */
public final class TotpMfaVerifier implements MfaVerifier {

	private static final String DEFAULT_SECRET_ATTRIBUTE_KEY = "otp_secret";
	private static final String DEFAULT_PROOF_CODE_KEY = "code";

	private final TotpVerifier totpVerifier;
	private final String secretAttributeKey;
	private final String proofCodeKey;

	public TotpMfaVerifier(TotpVerifier totpVerifier) {
		this(totpVerifier, DEFAULT_SECRET_ATTRIBUTE_KEY, DEFAULT_PROOF_CODE_KEY);
	}

	public TotpMfaVerifier(TotpVerifier totpVerifier, String secretAttributeKey, String proofCodeKey) {
		this.totpVerifier = Objects.requireNonNull(totpVerifier, "totpVerifier");
		this.secretAttributeKey = Strings.requireNonBlank(secretAttributeKey, "secretAttributeKey");
		this.proofCodeKey = Strings.requireNonBlank(proofCodeKey, "proofCodeKey");
	}

	@Override
	public boolean supports(MfaFactorType factorType) {
		return factorType == MfaFactorType.TOTP;
	}

	@Override
	public Optional<Map<String, Object>> verify(MfaVerificationRequest request, MfaEnrollment enrollment) {
		Objects.requireNonNull(request, "request");
		Objects.requireNonNull(enrollment, "enrollment");
		if (!supports(enrollment.getFactorType())) return Optional.empty();

		String secret = attributeValue(enrollment.getAttributes(), secretAttributeKey);
		String code = request.getProof(proofCodeKey).map(String::valueOf).orElse("");
		if (Strings.isBlank(secret) || Strings.isBlank(code)) return Optional.empty();
		if (!totpVerifier.verify(secret, code)) return Optional.empty();

		return Optional.of(Map.of(
			"verified_by", "totp",
			"totp_secret_attribute", secretAttributeKey
		));
	}

	private static String attributeValue(Map<String, Object> attributes, String key) {
		if (attributes == null || attributes.isEmpty()) return "";
		Object value = attributes.get(key);
		return value == null ? "" : String.valueOf(value);
	}
}
