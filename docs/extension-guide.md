# SPI 확장 가이드

## 확장 포인트 선택 기준

특정 URL, framework filter, 조직 header, gateway/internal boundary, 도메인 role 정책은 이 문서의 확장 대상이 아닙니다.

| 바꾸고 싶은 것 | 구현/교체할 계약 | 계층 경계 |
| --- | --- | --- |
| username으로 사용자 조회 | `UserFinder` | 사용자 저장소 연결까지만 담당 |
| password hash 검증 | `PasswordVerifier` | hash 알고리즘 검증까지만 담당 |
| access/refresh token 발급과 검증 | `TokenService` | 토큰 형식과 검증 기술까지만 담당 |
| JWT signing key 선택 | `JwtSigningKeyProvider`, `JwtKeyResolver` | `kid`, rotation, 외부 signer 연결까지만 담당 |
| JWT claim schema 변환 | `JwtClaimsMapper` | claim과 `Principal` 변환까지만 담당 |
| refresh token 저장 | `RefreshTokenStore` | 저장/조회/폐기까지만 담당 |
| refresh token rotation | `RefreshTokenRotationStrategy` | 이전 토큰 폐기와 다음 토큰 발급 흐름까지만 담당 |
| token revocation 상태 | `TokenRevocationStore` | `jti` 또는 opaque token 폐기 상태까지만 담당 |
| session 저장소 | `SessionStore`, `SessionRecordStore` | 저장소 구현과 session metadata까지만 담당 |
| session principal 변환 | `SessionPrincipalMapper` | 저장된 session 값을 `Principal`로 바꾸는 일까지만 담당 |
| session 만료 전략 | `SessionExpirationPolicy` | fixed/sliding expiration 계산까지만 담당 |
| 동시 session 처리 | `ConcurrentSessionPolicy` | 폐기할 session 선택까지만 담당 |
| JWT/session 조합 순서 | `HybridResolutionStrategy` | source priority 메커니즘까지만 담당 |
| JWT/session 충돌 처리 | `HybridConflictResolver` | 둘 다 성공했을 때 결과 선택/거부까지만 담당 |
| 새 인증 수단 provider | `AuthenticationProvider<C>` | credential을 검증해 `Principal`을 반환하는 일까지만 담당 |
| API key 인증 | `ApiKeyPrincipalResolver` | API key와 principal 매핑까지만 담당 |
| HMAC 인증 | `HmacSecretResolver`, `HmacSignatureVerifier` | secret 조회와 signature 검증까지만 담당 |
| OIDC 인증 | `OidcTokenVerifier`, `OidcPrincipalMapper` | ID token 검증과 principal 변환까지만 담당 |
| service account 인증 | `ServiceAccountVerifier` | service credential 검증까지만 담당 |

## 보안 주의점

- `TokenService` 구현은 토큰 서명, 검증 실패 처리, 만료 규칙을 명확히 가져가야 합니다.
- `JwtKeyResolver` 구현은 알 수 없는 `kid`를 조용히 기본 key로 fallback하지 않아야 합니다.
- `RefreshTokenStore` 구현은 여러 인스턴스 환경에서 안전하게 동작해야 합니다.
- `SessionStore` 구현은 세션 탈취를 막기 위해 저장소와 만료 정책을 일관되게 가져가야 합니다.
- `SessionPrincipalMapper` 구현은 세션 식별자와 사용자 주체를 섞지 않도록 책임을 분리해야 합니다.
- `OAuth2PrincipalResolver` 구현은 외부 provider 식별자를 내부 `Principal`로 바꾸는 경계만 담당해야 합니다.
- API key, HMAC, OIDC, service account provider는 인증 수단 자체만 검증해야 하며 URL, 헤더, gateway 정책을 알면 안 됩니다.

## 필수 구현

### 1) `UserFinder`

역할:

- username으로 인증 대상 사용자 조회

반환 모델:

- `User`

예시:

```java
public class AdminUserFinder implements UserFinder {
    private final UserRepository userRepository;

    public AdminUserFinder(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username)
            .map(e -> new User(
                String.valueOf(e.getId()),
                e.getUsername(),
                e.getPassword(),
                e.getAuthorities()
            ));
    }
}
```

## 선택 구현 또는 교체

### 2) `PasswordVerifier`

제공 형태:

- 기본 구현체를 강제하지 않는 SPI입니다.
- 실행 계층에서 bcrypt, argon2, legacy hash verifier 등을 주입합니다.

언제 교체하나:

- Argon2 등 다른 알고리즘을 쓸 때
- 레거시 해시 포맷과 호환해야 할 때

### 3) `TokenService`

기본값:

- `auth-jwt`의 `JwtTokenService`

언제 교체하나:

- issuer/audience/jti를 넣고 싶을 때
- asymmetric key, key rotation, external signer를 쓰고 싶을 때
- JWT가 아닌 다른 형식을 쓰고 싶을 때

관련 확장:

- `JwtSigningKeyProvider`: 발급 시 사용할 signing key 제공
- `JwtKeyResolver`: 검증 시 `kid` 기반 key 선택
- `JwtClaimsMapper`: claim schema와 `Principal` 변환
- `TokenRevocationStore`: `jti` 또는 opaque token 폐기 상태 저장
- `RefreshTokenRotationStrategy`: refresh token rotation 전략

### 4) `RefreshTokenStore`

제공 형태:

- 기본 저장소 구현체를 강제하지 않는 SPI입니다.
- 실행 계층에서 memory, Redis, RDB 구현체를 주입합니다.

운영 권장:

- Redis 기반 구현
- RDB 기반 구현

### 5) `OAuth2PrincipalResolver`

역할:

- Provider 사용자 정보를 내부 `Principal`로 매핑

예시:

```java
public class DefaultOAuth2PrincipalResolver implements OAuth2PrincipalResolver {
    @Override
    public Principal resolve(OAuth2UserIdentity identity) {
        User user = findOrCreateUser(identity);
        return new Principal(user.getUserId(), user.getRoles());
    }
}
```

### 6) Session 관련 확장 포인트

기본값:

- `SessionStore` - `SimpleSessionStore`
- `SessionRecordStore` - `SimpleSessionStore`
- `SessionPrincipalMapper` - `IdentitySessionPrincipalMapper`
- `SessionAuthenticationProvider` - `DefaultSessionAuthenticationProvider`
- `SessionExpirationPolicy` - fixed/sliding expiration 전략

언제 교체하나:

- 세션 저장소를 Redis/DB로 바꾸고 싶을 때
- 세션 principal에 추가 메타데이터를 주입하고 싶을 때
- sliding expiration, absolute timeout, concurrent session control이 필요할 때

보안 관점:

- 세션 식별자를 그대로 사용자 식별자로 쓰지 않습니다.
- 저장소 외부 노출이 가능한 값과 내부 주체 값을 분리합니다.
- 여러 인스턴스에서 일관된 만료 정책을 유지합니다.

### 7) Hybrid 관련 확장 포인트

기본값:

- `HybridAuthenticationProvider` - `DefaultHybridAuthenticationProvider`
- `HybridResolutionStrategy` - `SourceFirstHybridResolutionStrategy`
- `HybridConflictResolver` - `PreferFirstConflictResolver`

언제 교체하나:

- JWT와 session을 함께 시도하는 순서를 바꾸고 싶을 때
- source conflict 처리 방식을 바꾸고 싶을 때

보안 관점:

- JWT 우선, session 우선, 동일 principal만 허용 같은 조합 메커니즘은 1계층에 둘 수 있습니다.
- 브라우저, 외부 API, internal boundary별 적용 정책은 상위 계층에 둡니다.

### 8) 새 인증 capability

추가된 capability 모듈:

- `auth-apikey`: `ApiKeyAuthenticationProvider`, `ApiKeyPrincipalResolver`
- `auth-hmac`: `HmacAuthenticationProvider`, `HmacSecretResolver`, `HmacSignatureVerifier`
- `auth-oidc`: `OidcAuthenticationProvider`, `OidcTokenVerifier`, `OidcPrincipalMapper`
- `auth-service-account`: `ServiceAccountAuthenticationProvider`, `ServiceAccountVerifier`

구현 기준:

- provider는 credential 검증과 `Principal` 반환까지만 담당합니다.
- 어떤 요청에 provider를 적용할지는 이 저장소 밖에서 결정합니다.
- 특정 URL, 특정 header name, 특정 issuer/audience 기본값, 조직 role mapping은 넣지 않습니다.

## 권장 책임 분리

- Provider 설정, 회원 가입/계정 연결 정책, 리소스 permission policy, framework adapter는 애플리케이션이나 상위 계층이 소유합니다.
- `auth` 저장소는 principal 생성, 토큰 발급/검증, refresh rotation, 세션 처리, 인증 capability SPI에 집중합니다.
