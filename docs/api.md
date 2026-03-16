# API 가이드

컨트롤러 구현:
- `starter/src/main/java/com/auth/config/controller/AuthController.java`

기본 경로:
- `/auth`

## 1) 로그인

- Method/Path: `POST /auth/login`
- Request Body:

```json
{
  "username": "admin",
  "password": "plain-password"
}
```

- Response Body:

```json
{
  "accessToken": "..."
}
```

- 부가 동작:
  - refresh cookie 활성화 시 `Set-Cookie` 헤더에 refresh token 추가

## 2) 토큰 재발급

- Method/Path: `POST /auth/refresh`
- Request:
  - refresh cookie 필요 (`auth.refresh-cookie-name` 기준)
- Response Body:

```json
{ "accessToken": "..." }
```

- 부가 동작:
  - refresh rotation으로 새 refresh cookie 재발급

## 3) 로그아웃

- Method/Path: `POST /auth/logout`
- Request:
  - refresh cookie 필요
- Response:
  - `204 No Content`
- 부가 동작:
  - refresh 저장소 revoke
  - `Max-Age=0` 쿠키로 클라이언트 쿠키 만료

## 4) OAuth2 로그인 완료 응답

- 시작 경로 예시: `GET /oauth2/authorization/google`
- callback 경로 기본값: `GET /login/oauth2/code/{registrationId}`
- 동작 조건:
  - 서비스 애플리케이션에 `spring.security.oauth2.client.registration.*` 설정 존재
  - `OAuth2PrincipalResolver` 빈 등록
  - `auth.oauth2.enabled=true`

- 성공 응답:

```json
{
  "accessToken": "..."
}
```

- 부가 동작:
  - refresh cookie 활성화 시 `Set-Cookie` 헤더에 refresh token 추가

## 에러 처리

비즈니스 오류는 `AuthException` + `ErrorCode`로 표현됩니다.

대표 케이스:
- `INVALID_REQUEST`: 필수 인자/쿠키 누락
- `USER_NOT_FOUND`: 사용자 조회 실패
- `INVALID_CREDENTIALS`: 비밀번호 불일치
- `INVALID_TOKEN`: 토큰 서명/형식/타입 문제
- `TOKEN_REVOKED`: 저장소 기준 폐기된 refresh 토큰

에러코드 정의:
- `contract/src/main/java/com/auth/api/exception/ErrorCode.java`

## 인증 헤더 사용

보호된 API는 다음 형식으로 access token을 보냅니다.

```http
Authorization: Bearer <access-token>
```

접두어는 `auth.bearer-prefix`로 변경할 수 있습니다.
