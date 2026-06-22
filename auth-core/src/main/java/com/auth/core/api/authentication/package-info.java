/**
 * 인증 처리의 공통 계약을 정의하는 패키지입니다.
 *
 * <p>이 패키지는 자격 증명을 검증해 {@code Principal}을 반환하는 제공자 인터페이스를
 * 제공합니다. 구현 모듈은 이 계약을 기준으로 JWT, 세션, API Key 같은 인증 방식을
 * 일관된 형태로 노출합니다.
 *
 * <p>주요 타입:
 * <ul>
 *   <li>{@link com.auth.core.api.authentication.AuthenticationProvider}</li>
 * </ul>
 */
package com.auth.core.api.authentication;
