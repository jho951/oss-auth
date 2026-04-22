package com.auth.api.exception;

/**
 * 실패 원인을 분류하기 위한 최소 수준
 * <p>
 * 이 값은 외부 API 응답 코드 계약을 의미하지 않으며,
 * HTTP 상태 코드나 응답 포맷 매핑은 서비스 애플리케이션이 담당합니다.
 * </p>
 */
public enum AuthFailureReason {
	/** 형식이 잘못됨 (예: 이메일 양식 오류) */
	INVALID_INPUT,
	/** 없는 사용자 */
	USER_NOT_FOUND,
	/** 틀린 비밀번호 */
	INVALID_CREDENTIALS,
	/** 변조된 토큰이나 만료된 토큰 */
	INVALID_TOKEN,
	/** 토큰은 맞는데 정지된 토큰 (예: 탈취 신고됨) */
	REVOKED_TOKEN,
	/** 서버 내부 오류 (예: DB 장애) */
	INTERNAL
}
