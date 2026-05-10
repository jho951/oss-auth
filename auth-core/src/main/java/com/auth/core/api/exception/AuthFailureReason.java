package com.auth.core.api.exception;

/** 실패 원인을 분류*/
public enum AuthFailureReason {
	/** 형식이 잘못됨 (예: 이메일 양식 오류) */
	INVALID_INPUT,
	/** 없는 사용자 */
	USER_NOT_FOUND,
	/** 틀린 비밀번호 */
	INVALID_CREDENTIALS,
	/** 사용자 계정이 비활성 상태 */
	USER_DISABLED,
	/** 사용자 계정이 잠긴 상태 */
	USER_LOCKED,
	/** 사용자 계정이 만료된 상태 */
	USER_EXPIRED,
	/** 비밀번호 등 자격 증명이 만료된 상태 */
	CREDENTIALS_EXPIRED,
	/** 변조된 토큰이나 만료된 토큰 */
	INVALID_TOKEN,
	/** 토큰은 맞는데 정지된 토큰 (예: 탈취 신고됨) */
	REVOKED_TOKEN,
	/** 서버 내부 오류 (예: DB 장애) */
	INTERNAL
}
