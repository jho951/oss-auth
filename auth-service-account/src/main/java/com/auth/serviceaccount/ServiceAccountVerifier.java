package com.auth.serviceaccount;

import com.auth.core.api.model.Principal;
import java.util.Optional;

/** 서비스 계정 자격 증명을 검증하고 서비스 principal을 반환합니다. */
public interface ServiceAccountVerifier {

	Optional<Principal> verify(ServiceAccountCredential credential);
}
