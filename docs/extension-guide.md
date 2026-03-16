# SPI 확장 가이드

`core`는 구현체를 직접 모르고 `spi` 인터페이스로만 동작합니다.
애플리케이션은 아래 SPI를 구현해서 주입해야 합니다.

인터페이스 위치:
- `spi/src/main/java/com/auth/spi/UserFinder.java`
- `spi/src/main/java/com/auth/spi/PasswordVerifier.java`
- `spi/src/main/java/com/auth/spi/TokenService.java`
- `spi/src/main/java/com/auth/spi/RefreshTokenStore.java`

## 필수 구현

## 1) `UserFinder`

역할:
- username으로 인증 대상 사용자 조회

반환 모델:
- `contract/src/main/java/com/auth/api/model/User.java`

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
				e.getRoles()
			));
	}
}
```

## 2) `RefreshTokenStore`

역할:
- refresh 저장/조회/폐기

권장 구현:
- Redis: TTL 기반 만료 처리 용이
- RDB: 사용자별, 디바이스별 정책 세분화 가능

최소 계약:
- `save(userId, refreshToken, expiresAt)`
- `exists(userId, refreshToken)`
- `revoke(userId, refreshToken)`

## 선택 구현

## 3) `PasswordVerifier`

- Spring Security BCrypt가 classpath에 있으면 기본 구현이 자동 제공됩니다.
- 다른 알고리즘을 쓰려면 직접 빈을 등록해 교체할 수 있습니다.

## 4) `TokenService`

- `auth.jwt.secret`가 있으면 기본 `JwtTokenService`가 자동 등록됩니다.
- 커스텀 JWT 정책(jti, issuer, audience, key rotation 등)이 필요하면 구현체를 직접 등록합니다.

## OAuth2/OIDC 연동 권장 방식

- Google, GitHub, Kakao 같은 Provider 연동 설정은 이 모듈이 아니라 각 서비스 애플리케이션에서 처리하는 것이 맞습니다.
- 서비스 애플리케이션은 Spring Security OAuth2 Client 등으로 Provider 인증을 끝낸 뒤, 내부 사용자 식별자와 권한을 결정해야 합니다.
- 서비스 애플리케이션이 `OAuth2PrincipalResolver` 빈을 등록하면, starter가 OAuth2 로그인 성공 후 내부 사용자 매핑과 JWT 응답까지 자동 처리할 수 있습니다.
- 내부 사용자 매핑이 끝나면 `AuthService.login(Principal)`을 호출해 이 모듈의 access/refresh 발급 흐름을 재사용할 수 있습니다.

예시:

```java
Principal principal = new Principal(internalUser.getId(), internalUser.getRoles());
Tokens tokens = authService.login(principal);
```

Resolver 예시:

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

권장 책임 분리:

- 서비스 애플리케이션:
  - Provider 설정(`client-id`, `client-secret`, `redirect-uri`)
  - callback 처리
  - Provider 사용자 정보 조회
  - 회원 가입/계정 연결 정책
- auth-module:
  - 내부 사용자 기준 JWT 발급
  - refresh rotation
  - access token 검증 필터

## Auto Configuration 교체 규칙

`starter`의 기본 빈들은 대부분 `@ConditionalOnMissingBean`입니다.
즉, 같은 타입 빈을 애플리케이션에서 먼저 등록하면 기본 구현을 덮어쓸 수 있습니다.

해당 클래스:
- `starter/src/main/java/com/auth/config/AuthAutoConfiguration.java`
