# OAuth2 Starter Design

## 목표

- 이 모듈이 기존 JWT 인증 기능을 유지하면서 OAuth2/OIDC 로그인 완료 처리까지 제공
- Provider별 상세 설정은 서비스 애플리케이션에 남기고, 공통 후처리만 모듈에 포함
- OAuth2 성공 후 내부 사용자 매핑과 access/refresh 발급을 표준화

## 책임 분리

### 서비스 애플리케이션

- `spring.security.oauth2.client.registration.*` 설정
- Google, GitHub, Kakao 등 Provider 선택
- 클라이언트 ID/시크릿, redirect URI 관리
- 내부 회원 가입/계정 연결 정책 구현
- `OAuth2PrincipalResolver` 구현

### auth-module

- OAuth2 로그인 성공/실패 핸들러 제공
- 외부 사용자 정보를 내부 `Principal`로 바꾸는 SPI 제공
- 내부 `Principal` 기준 access/refresh JWT 발급
- refresh cookie 작성과 refresh rotation 재사용

## 설계 포인트

### 1. 중립 모델

`contract`에 `OAuth2UserIdentity`를 추가합니다.

- `provider`
- `providerUserId`
- `email`
- `name`
- `attributes`

이 모델은 Spring Security 클래스에 직접 의존하지 않아서 `spi`와 `core`에서도 안전하게 사용할 수 있습니다.

### 2. 확장 포트

`spi`에 `OAuth2PrincipalResolver`를 추가합니다.

```java
public interface OAuth2PrincipalResolver {
    Principal resolve(OAuth2UserIdentity identity);
}
```

서비스 애플리케이션은 이 포트를 구현해 다음 정책을 처리합니다.

- 기존 계정 연결
- 신규 회원 생성
- 허용되지 않은 사용자 차단
- 역할 결정

### 3. 토큰 발급 재사용

`AuthService.login(Principal)` 을 통해 이미 인증된 사용자에 대해 기존 JWT 발급 흐름을 재사용합니다.

이 경로는 다음을 그대로 사용합니다.

- access token 발급
- refresh token 발급
- refresh token 저장
- refresh cookie 작성

### 4. Starter OAuth2 자동 구성

`starter`는 OAuth2 client 의존성이 있고 `OAuth2PrincipalResolver` 빈이 존재할 때만 OAuth2 자동 구성을 활성화합니다.

활성 시:

- `oauth2Login()` 활성화
- 성공 핸들러: `OAuth2AuthenticationSuccessHandler`
- 실패 핸들러: `OAuth2AuthenticationFailureHandler`
- `/oauth2/**`, `/login/oauth2/**` 또는 설정된 OAuth2 경로 허용

## 요청 흐름

1. 클라이언트가 `/oauth2/authorization/{provider}` 접근
2. Spring Security OAuth2 Client가 Provider 인증 수행
3. 인증 성공 시 `OAuth2AuthenticationSuccessHandler` 실행
4. Provider 사용자 정보를 `OAuth2UserIdentity` 로 변환
5. `OAuth2PrincipalResolver` 가 내부 `Principal` 반환
6. `AuthService.login(Principal)` 이 access/refresh 발급
7. 응답 바디에 access token, 쿠키에 refresh token 작성

## 응답 형태

성공:

```http
200 OK
Set-Cookie: refresh_token=...; HttpOnly; Path=/; SameSite=Lax
Content-Type: application/json

{"accessToken":"..."}
```

실패:

```http
401 Unauthorized
Content-Type: application/json

{"message":"OAUTH2_AUTHENTICATION_FAILED"}
```

## 설정 키

`auth.oauth2.*`

- `enabled`
- `authorization-base-uri`
- `login-processing-base-uri`

Provider 등록 값은 계속 `spring.security.oauth2.client.*` 아래에서 관리합니다.

## 예시

```java
@Component
public class DefaultOAuth2PrincipalResolver implements OAuth2PrincipalResolver {

    private final UserRepository userRepository;

    public DefaultOAuth2PrincipalResolver(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Principal resolve(OAuth2UserIdentity identity) {
        UserEntity user = userRepository.findOrCreate(identity.getProvider(), identity.getProviderUserId(), identity.getEmail());
        return new Principal(String.valueOf(user.getId()), user.getRoles());
    }
}
```

## 제약

- Provider별 세부 attribute 구조 차이는 서비스 애플리케이션이 처리해야 합니다.
- Kakao처럼 중첩 JSON 구조를 쓰는 경우 `identity.getAttributes()` 를 직접 해석해야 합니다.
- 현재 성공 응답은 JSON 기반입니다. 프론트엔드 redirect 전략이 필요하면 성공 핸들러를 교체하면 됩니다.

Provider별 실제 적용 예시는 아래 문서를 참고합니다.

- Google: `docs/oauth2-google-quickstart.md`
- GitHub: `docs/oauth2-github-quickstart.md`
- Kakao: `docs/oauth2-kakao-quickstart.md`
