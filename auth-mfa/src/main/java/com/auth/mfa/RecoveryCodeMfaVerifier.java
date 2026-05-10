package com.auth.mfa;

import com.auth.core.utils.Strings;
import com.auth.otp.Sha256RecoveryCodeVerifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 복구 코드 해시 목록과 사용자가 제출한 코드를 비교하는 MFA 검증기입니다.
 *
 * <p>코드 소모 처리 자체는 저장소 갱신이 필요한 상위 계층 책임이므로,
 * 이 구현은 일치 여부와 어떤 해시가 맞았는지만 반환합니다.
 */
public final class RecoveryCodeMfaVerifier implements MfaVerifier {

	private static final String DEFAULT_HASHES_ATTRIBUTE_KEY = "recovery_hashes";
	private static final String DEFAULT_PROOF_CODE_KEY = "code";

	private final Sha256RecoveryCodeVerifier recoveryCodeVerifier;
	private final String hashesAttributeKey;
	private final String proofCodeKey;

	public RecoveryCodeMfaVerifier(Sha256RecoveryCodeVerifier recoveryCodeVerifier) {
		this(recoveryCodeVerifier, DEFAULT_HASHES_ATTRIBUTE_KEY, DEFAULT_PROOF_CODE_KEY);
	}

	public RecoveryCodeMfaVerifier(
		Sha256RecoveryCodeVerifier recoveryCodeVerifier,
		String hashesAttributeKey,
		String proofCodeKey
	) {
		this.recoveryCodeVerifier = Objects.requireNonNull(recoveryCodeVerifier, "recoveryCodeVerifier");
		this.hashesAttributeKey = Strings.requireNonBlank(hashesAttributeKey, "hashesAttributeKey");
		this.proofCodeKey = Strings.requireNonBlank(proofCodeKey, "proofCodeKey");
	}

	@Override
	public boolean supports(MfaFactorType factorType) {
		return factorType == MfaFactorType.RECOVERY_CODE;
	}

	@Override
	public Optional<Map<String, Object>> verify(MfaVerificationRequest request, MfaEnrollment enrollment) {
		Objects.requireNonNull(request, "request");
		Objects.requireNonNull(enrollment, "enrollment");
		if (!supports(enrollment.getFactorType())) return Optional.empty();

		String code = request.getProof(proofCodeKey).map(String::valueOf).orElse("");
		List<String> hashes = hashes(enrollment.getAttributes().get(hashesAttributeKey));
		if (Strings.isBlank(code) || hashes.isEmpty()) return Optional.empty();
		if (!recoveryCodeVerifier.verify(code, hashes)) return Optional.empty();

		return Optional.of(Map.of(
			"verified_by", "recovery_code",
			"matched_recovery_code_hash", recoveryCodeVerifier.hash(code)
		));
	}

	private static List<String> hashes(Object rawValue) {
		if (rawValue == null) return List.of();
		if (rawValue instanceof Iterable<?> iterable) {
			ArrayList<String> values = new ArrayList<>();
			for (Object item : iterable) {
				if (item != null) {
					String value = String.valueOf(item);
					if (!value.isBlank()) values.add(value);
				}
			}
			return List.copyOf(values);
		}
		if (rawValue.getClass().isArray() && rawValue instanceof Object[] array) {
			ArrayList<String> values = new ArrayList<>();
			for (Object item : array) {
				if (item != null) {
					String value = String.valueOf(item);
					if (!value.isBlank()) values.add(value);
				}
			}
			return List.copyOf(values);
		}
		String singleValue = String.valueOf(rawValue);
		return singleValue.isBlank() ? List.of() : List.of(singleValue);
	}
}
