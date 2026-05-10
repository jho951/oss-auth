/**
 * JWT 처리 구현을 확장하기 위한 SPI를 정의하는 패키지입니다.
 *
 * <p>claim 직렬화 규칙, 서명 키 선택, 발급 키 제공 방식처럼
 * JWT 인프라 의존성이 달라질 수 있는 부분을 인터페이스와 기본 구현으로 분리합니다.
 */
package com.auth.support.jwt.spi;
