# SPI 확장 가이드

## 인터페이스 위치

- `auth-core/src/main/java/com/auth/spi/UserFinder.java`
- `auth-core/src/main/java/com/auth/spi/PasswordVerifier.java`
- `auth-core/src/main/java/com/auth/spi/TokenService.java`
- `auth-core/src/main/java/com/auth/spi/RefreshTokenStore.java`
- `auth-core/src/main/java/com/auth/spi/OAuth2PrincipalResolver.java`
- `auth-session/src/main/java/com/auth/session/SessionStore.java`
- `auth-session/src/main/java/com/auth/session/SessionPrincipalMapper.java`
- `auth-hybrid/src/main/java/com/auth/hybrid/HybridAuthenticationProvider.java`

## 보안 주의점

- `TokenService` 구현은 토큰 서명, 검증 실패 처리, 만료 규칙을 명확히 가져가야 합니다.
- `RefreshTokenStore` 구현은 여러 인스턴스 환경에서 안전하게 동작해야 합니다.
- `SessionStore` 구현은 세션 탈취를 막기 위해 저장소와 만료 정책을 일관되게 가져가야 합니다.
- `SessionPrincipalMapper` 구현은 세션 식별자와 사용자 주체를 섞지 않도록 책임을 분리해야 합니다.
- `OAuth2PrincipalResolver` 구현은 외부 provider 식별자를 내부 `Principal`로 바꾸는 경계만 담당해야 합니다.

## 필수 구현

### 1) `UserFinder`

역할:

- username으로 인증 대상 사용자 조회

반환 모델:

- `auth-core/src/main/java/com/auth/api/model/User.java`

예시:

```java
@Component
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

기본값:

- `BCryptPasswordVerifier`

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

### 4) `RefreshTokenStore`

기본값:

- `InMemoryRefreshTokenStore`

운영 권장:

- Redis 기반 구현
- RDB 기반 구현

### 5) `OAuth2PrincipalResolver`

역할:

- Provider 사용자 정보를 내부 `Principal`로 매핑

예시:

```java
@Component
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
- `SessionPrincipalMapper` - `IdentitySessionPrincipalMapper`
- `SessionAuthenticationProvider` - `DefaultSessionAuthenticationProvider`

언제 교체하나:

- 세션 저장소를 Redis/DB로 바꾸고 싶을 때
- 세션 principal에 추가 메타데이터를 주입하고 싶을 때

보안 관점:

- 세션 식별자를 그대로 사용자 식별자로 쓰지 않습니다.
- 저장소 외부 노출이 가능한 값과 내부 주체 값을 분리합니다.
- 여러 인스턴스에서 일관된 만료 정책을 유지합니다.

### 7) Hybrid 관련 확장 포인트

기본값:

- `HybridAuthenticationProvider` - `DefaultHybridAuthenticationProvider`

언제 교체하나:

- JWT와 session을 함께 시도하는 순서를 바꾸고 싶을 때
- 추가 인증 경로를 넣고 싶을 때

보안 관점:

- JWT가 유효하면 세션보다 먼저 받아들이는지, 반대로 세션을 우선하는지 정책을 명확히 합니다.
- 조합 우선순위는 서비스마다 다를 수 있으므로, 교체 가능하게 유지합니다.

## 권장 책임 분리

- Provider 설정, 회원 가입/계정 연결 정책, 리소스 permission policy는 애플리케이션이 소유합니다.
- `auth` 저장소는 principal 생성, 토큰 발급, refresh rotation, access token 검증 필터에 집중합니다.
