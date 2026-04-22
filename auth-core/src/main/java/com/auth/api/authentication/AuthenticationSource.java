package com.auth.api.authentication;

/** 인증 근거(경로)*/
public enum AuthenticationSource {
	/** 정보 암호화 토큰 (JSON Web Token) */
	JWT,
	/** 서버 메모리에 사용자 정보를 저장 (Session_ID) */
	SESSION,
	/** 서버와 클라이언트가 미리 약속한 Key를 매 요청마다 전송 */
	API_KEY,
	/** 메시지 내용과 비밀 키를 섞은 특수한 해시값 (Hash-based Message Authentication Code) */
	HMAC,
	/** 서버와 클라이언트가 인증서를 서버에 제출해 양방향 통신 확인 (Mutual TLS) */
	MTLS,
	/** 직접 비밀번호를 관리하지 않고, 외부 인증 기관을 통해 정보를 받아오는 프로토콜 (OpenID Connect) */
	OIDC,
	/**  서버 프로그램이나 자동화 스크립트가 시스템에 접근하기 위해 사용하는 특수 계정 */
	SERVICE_ACCOUNT,
	/** 로그인이나 특정 작업을 할 때 딱 한 번만 쓸 수 있고 사라지는 토큰 */
	ONE_TIME_TOKEN,
	/** 시스템이 인식하지 못하는 방식, 로그 기록이 누락되었을 때 사용하는 예외 처리용 값 */
	UNKNOWN
}
