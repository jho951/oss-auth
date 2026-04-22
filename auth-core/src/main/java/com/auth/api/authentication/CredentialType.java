package com.auth.api.authentication;

/** 인증 수단(증거) */
public enum CredentialType {
	/** JWT 같은 토큰을 HTTP 헤더에 담아 보낼 때 사용 (사실 상 Access Token)*/
	BEARER_TOKEN,
	/** 로그인 유지를 위해 사용하는 유효기간이 긴 토큰 (새 Access Token을 발급받을 때 제출) */
	REFRESH_TOKEN,
	/** 브라우저 쿠키에 담겨 오는 JSESSIONID 같은 고유 식별 번호 */
	SESSION_ID,
	/** 서버에서 발급해준 긴 문자열 키 */
	API_KEY,
	/**  메시지를 암호화해서 만든 '디지털 서명' 값 */
	HMAC_SIGNATURE,
	/** MTLS에서 사용하는 클라이언트의 '전자 인증서' 파일 */
	CLIENT_CERTIFICATE,
	/** OICD 성공 후 받아온 사용자의 신원 정보가 담긴 토큰 */
	OIDC_ID_TOKEN,
	/** 서비스 계정 전용 비밀번호나 비밀 키 */
	SERVICE_ACCOUNT_SECRET,
	/** 일회용 비밀번호(OTP)나 일회용 링크에 담긴 토큰값 */
	ONE_TIME_TOKEN
}
