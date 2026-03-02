# 아키텍처 개요

## 설계 목표

- 인증 도메인 로직을 프레임워크/DB 구현에서 분리
- 모듈화로 재사용성과 교체 가능성 확보
- Spring Boot 환경에서는 자동 설정으로 빠른 적용

## 레이어 구조

- `contract`: 외부에 노출되는 모델/예외/에러코드
- `spi`: 코어가 의존하는 포트(인터페이스)
- `core`: 인증 유즈케이스(`AuthService`)
- `starter`: Spring Boot 자동 설정, REST API, Security 필터
- `common`: 공용 유틸
## 요청 처리 흐름

## 1) 로그인

1. 클라이언트가 `POST /auth/login` 호출
2. `AuthController`가 `AuthService.login` 실행
3. `AuthService`가 `UserFinder`, `PasswordVerifier`로 인증
4. `TokenService`로 access/refresh 발급
5. `RefreshTokenStore`에 refresh 저장
6. `RefreshCookieWriter`가 refresh 쿠키를 `Set-Cookie`로 작성
7. 응답 바디에는 access token 반환

관련 코드:
- `starter/src/main/java/com/auth/config/controller/AuthController.java`
- `core/src/main/java/com/auth/core/service/AuthService.java`

## 2) 토큰 재발급 (Refresh Rotation)

1. 클라이언트가 `POST /auth/refresh` 호출
2. 컨트롤러가 refresh cookie 추출
3. `AuthService.refresh`가 refresh 토큰 서명/타입 검증
4. 저장소 존재 여부 확인(`exists`)
5. 기존 refresh 폐기(`revoke`)
6. 새 access/refresh 발급 후 새 refresh 저장(`save`)
7. 새 refresh를 쿠키로 갱신

## 3) 로그아웃

1. 클라이언트가 `POST /auth/logout` 호출
2. 컨트롤러가 refresh cookie 추출
3. `AuthService.logout`이 저장소에서 refresh 폐기
4. `RefreshCookieWriter.clear`가 만료 쿠키(`Max-Age=0`) 전송

## 인증 필터 흐름

1. `AuthOncePerRequestFilter`가 `Authorization` 헤더 확인
2. `auth.bearer-prefix`로 시작하면 access token 파싱
3. `TokenService.verifyAccessToken`으로 `Principal` 복원
4. SecurityContext에 인증 객체 저장

관련 코드:
- `starter/src/main/java/com/auth/config/security/AuthOncePerRequestFilter.java`

## 자동 구성 진입점

Spring Boot 자동 구성 등록 파일:
- `starter/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

등록 클래스:
- `com.auth.config.AuthAutoConfiguration`
- `com.auth.config.security.AuthSecurityAutoConfiguration`
