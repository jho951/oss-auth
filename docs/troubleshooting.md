# 트러블슈팅

## 1) `auth.jwt.secret must not be blank`

원인:
- `auth.jwt.secret` 미설정

해결:
- `application.yml` 또는 환경변수에 `auth.jwt.secret` 설정

## 2) `auth.jwt.secret must be at least 32 bytes for HS256`

원인:
- 시크릿 길이 부족

해결:
- 최소 32바이트 이상 문자열 사용

## 3) `refresh cookie not found`

원인:
- `/auth/refresh` 또는 `/auth/logout` 요청에 refresh cookie 누락
- 쿠키 이름이 `auth.refresh-cookie-name`과 다름

해결:
- 브라우저/클라이언트에서 쿠키 포함 요청 확인
- 서버 설정과 실제 쿠키명 일치 확인

## 4) `TOKEN_REVOKED`

원인:
- 저장소 기준으로 refresh가 이미 폐기됨
- refresh rotation 이후 이전 토큰 재사용

해결:
- 최신 refresh cookie로 재시도
- 필요 시 재로그인으로 새 토큰 발급

## 5) 보호 API에서 항상 401

원인:
- `Authorization` 헤더 누락
- `auth.bearer-prefix`와 헤더 접두사 불일치
- access token 만료/서명 불일치

해결:
- `Authorization: Bearer <token>` 형식 확인
- prefix 커스텀 시 클라이언트 동기화

## 6) 로컬 테스트 중 Spring HTTP 클래스 미해결

원인:
- 모듈 테스트 클래스패스에 web 의존성 누락

해결:
- `starter/build.gradle`에 테스트용 web 의존성 추가
  - `testImplementation "org.springframework.boot:spring-boot-starter-web:${springBootVersion}"`

## 7) publish 단계 인증 실패

원인:
- Maven Central credentials/서명키 미설정 또는 권한 부족

해결:
- `MAVEN_CENTRAL_USERNAME`, `MAVEN_CENTRAL_PASSWORD` 설정
- `MAVEN_CENTRAL_GPG_PRIVATE_KEY`, `MAVEN_CENTRAL_GPG_PASSPHRASE` 설정
- Sonatype(OSSRH) 권한 확인
