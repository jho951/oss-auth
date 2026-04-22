package com.auth.api.model;

/** 시스템에 접속한 주체가 종류 */
public enum PrincipalType {
	/** 사람 사용자 (로그인한 회원) */
	USER,
	/** 프로그램이나 서버 (Grafana 모니터링 로봇) */
	SERVICE,
	/** 로그인하지 않은 익명 방문자 (아무 권한 없는 기본 상태) */
	ANONYMOUS,
	/** 대리인 (고객 계정에 접근)*/
	DELEGATED,
	/** 사용자 계정 가정 (오류 재현) */
	IMPERSONATION,
	/** 정보가 없는 상태, 예외 */
	UNKNOWN
}
