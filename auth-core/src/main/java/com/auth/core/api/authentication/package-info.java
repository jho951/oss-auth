/**
 * 인증 처리의 공통 계약을 정의하는 패키지입니다.
 *
 * <p>이 패키지는 자격 증명을 검증하는 제공자 인터페이스, 인증 성공 결과, 그리고 인증 수단 분류를
 * 함께 제공합니다. 구현 모듈은 이 계약을 기준으로 JWT, 세션, API Key 같은 인증 방식을 일관된
 * 형태로 노출합니다.
 *
 * <p>주요 타입:
 * <ul>
 *   <li>{@link com.auth.core.api.authentication.AuthenticationProvider}</li>
 *   <li>{@link com.auth.core.api.authentication.AuthenticationResult}</li>
 *   <li>{@link com.auth.core.api.authentication.AuthenticationSource}</li>
 *   <li>{@link com.auth.core.api.authentication.CredentialType}</li>
 * </ul>
 */
package com.auth.core.api.authentication;
