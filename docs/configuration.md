# 설정 레퍼런스

## 전제

- Spring Boot 자동 설정은 `starter` 모듈 포함 시 활성화됩니다.
- 프로퍼티 바인딩 클래스:
  - `starter/src/main/java/com/auth/config/AuthProperties.java`
  - `starter/src/main/java/com/auth/config/jwt/AuthJwtProperties.java`

## `auth.*`

| 키 | 기본값 | 설명 |
| --- | --- | --- |
| `auth.endpoints-enabled` | `true` | `/auth/*` 컨트롤러 자동 등록 여부 |
| `auth.bearer-prefix` | `Bearer ` | `Authorization` 헤더 접두사 |
| `auth.refresh-cookie-enabled` | `true` | refresh 쿠키 사용 여부 |
| `auth.refresh-cookie-name` | `refresh_token` | refresh 쿠키 이름 |
| `auth.refresh-cookie-http-only` | `true` | HttpOnly 속성 |
| `auth.refresh-cookie-secure` | `true` | Secure 속성 |
| `auth.refresh-cookie-path` | `/` | 쿠키 path |
| `auth.refresh-cookie-same-site` | `Lax` | SameSite 속성 |
| `auth.auto-security` | `true` | 기본 SecurityFilterChain 자동 구성 여부 |

## `auth.jwt.*`

| 키 | 기본값 | 설명 |
| --- | --- | --- |
| `auth.jwt.secret` | 없음 | JWT 서명 키(필수, HS256 최소 32바이트 권장) |
| `auth.jwt.access-seconds` | `900` | access token 만료(초) |
| `auth.jwt.refresh-seconds` | `1209600` | refresh token 만료(초) |

## `auth.oauth2.*`

| 키 | 기본값 | 설명 |
| --- | --- | --- |
| `auth.oauth2.enabled` | `true` | OAuth2 starter 자동 구성 활성화 여부 |
| `auth.oauth2.authorization-base-uri` | `/oauth2/authorization` | OAuth2 로그인 시작 경로 |
| `auth.oauth2.login-processing-base-uri` | `/login/oauth2/code/*` | Provider callback 처리 경로 |

## 최소 설정 예시

```yaml
auth:
  refresh-cookie-name: "ADMIN_REFRESH_TOKEN"
  oauth2:
    enabled: true
  jwt:
    secret: ${AUTH_JWT_SECRET}
    access-seconds: 3600
    refresh-seconds: 1209600
```

## 주의사항

- `auth.jwt.secret`가 없으면 기본 `TokenService`(`JwtTokenService`) 자동 등록이 되지 않습니다.
- `auth.jwt.secret` 길이가 32바이트 미만이면 앱 시작/토큰 생성 시 예외가 발생할 수 있습니다.
- `auth.refresh-cookie-secure=true`는 HTTPS 환경에서 사용하는 것이 안전합니다.
- `auth.refresh-cookie-enabled=false`로 설정하면 refresh는 쿠키가 아닌 다른 방식으로 전달해야 합니다.
- OAuth2 provider 등록 정보는 `auth.oauth2.*`가 아니라 `spring.security.oauth2.client.*` 에서 설정해야 합니다.

## refresh 수명 동기화

`auth.jwt.refresh-seconds`는 다음 동작에 사용됩니다.

- refresh JWT 만료 시간
- `AuthService` 내부 refresh TTL (`Duration.ofSeconds(...)`)
- refresh cookie `Max-Age`
