## 🚀 목표

- 인증 로직을 애플리케이션 코드에서 분리
- JWT 기반 토큰 인증의 표준화
- 서비스별 사용자 저장소(DB) 차이를 SPI로 분리
- 설정(application.yml)만으로 빠른 적용

---

## 🧱 프로젝트 구조
``` text
├─ auth-core
├─ auth-common-test
├─ auth-jwt
├─ auth-session
├─ auth-hybrid
├─ auth-spring
├─ auth-jwt-spring-boot-starter
├─ auth-session-spring-boot-starter
├─ auth-hybrid-spring-boot-starter
├─ samples
│  ├─ sample-jwt-api
│  ├─ sample-session-web
│  └─ sample-hybrid-sso
└─ docs
```
---

## 📚 문서

- 문서 진입점: [docs/README.md](./docs/README.md)
- 아키텍처 개요: [docs/architecture.md](./docs/architecture.md)
- 모듈 가이드: [docs/modules.md](./docs/modules.md)
- 설정 레퍼런스: [docs/configuration.md](./docs/configuration.md)
- API 가이드: [docs/api.md](./docs/api.md)
- 보안 동작: [docs/security.md](./docs/security.md)
- SPI 확장 가이드: [docs/extension-guide.md](./docs/extension-guide.md)
- OAuth2 Starter 설계: [docs/oauth2-design.md](./docs/oauth2-design.md)
- Redis RefreshTokenStore 가이드: [docs/redis-refresh-token-store.md](./docs/redis-refresh-token-store.md)
- Google OAuth2 빠른 시작: [docs/oauth2-google-quickstart.md](./docs/oauth2-google-quickstart.md)
- GitHub OAuth2 빠른 시작: [docs/oauth2-github-quickstart.md](./docs/oauth2-github-quickstart.md)
- Kakao OAuth2 빠른 시작: [docs/oauth2-kakao-quickstart.md](./docs/oauth2-kakao-quickstart.md)
- 테스트/CI 가이드: [docs/testing-and-ci.md](./docs/testing-and-ci.md)
- 릴리즈 가이드: [docs/release.md](./docs/release.md)
- 트러블슈팅: [docs/troubleshooting.md](./docs/troubleshooting.md)
- RefreshCookieWriter 상세: [docs/refresh-cookie-writer.md](./docs/refresh-cookie-writer.md)

---

## 📦 모듈 (Modules)
> 새로운 레이어드 아키텍처로 구성되며, 책임 중심으로 필요한 부분만 선택하여 사용합니다.

| Module | 설명 |
| --- | --- |
| `auth-core` | 모델(`Principal`, `Tokens`, `User`, `OAuth2UserIdentity`), 예외, SPI(`UserFinder`, `TokenService` 등)와 `AuthService`를 제공하며, 권한 메타데이터는 `authorities`/`attributes`로만 전달합니다. |
| `auth-common-test` | 테스트 픽스처(`AuthTestFixtures`)와 샘플/통합 테스트용 `InMemoryRefreshTokenStore`를 담습니다. |
| `auth-jwt` | JWT 전용 `TokenService` 구현(`JwtTokenService`)과 관련 유틸을 갖춰 `auth-core` 위에서 동작합니다. |
| `auth-session` | 세션 저장소/매퍼/추출기 추상(`SessionStore`, `SessionAuthenticationProvider`, `SessionPrincipalMapper`, `SessionCookieExtractor`)과 `SimpleSessionStore`를 제공합니다. |
| `auth-hybrid` | JWT와 세션을 조합하는 추상(`HybridAuthenticationProvider`, `CompositeAuthenticationProvider`)을 제공합니다. |
| `auth-spring` | `AuthProperties` 같은 Spring 설정만 제공하는 브리지 모듈로, 구체적 쿠키/필터 구현은 스타터에서 담당합니다. |
| `auth-jwt-spring-boot-starter` | `AuthAutoConfiguration`, `AuthSecurityAutoConfiguration`, `AuthJwtProperties`로 JWT + Spring 보안 자동 설정을 구성하고, `AuthOncePerRequestFilter`, `RefreshTokenExtractor`, `BCryptPasswordVerifier`, `RefreshTokenStore`(InMemory) 등을 등록합니다. |
| `auth-session-spring-boot-starter` | `AuthSessionAutoConfiguration`으로 기본 세션 빈(`SessionStore`, `SessionService`, `SessionAuthenticationFilter`)을 제공합니다. |
| `auth-hybrid-spring-boot-starter` | OAuth2 로그인 + JWT 발급 흐름(`AuthOAuth2AutoConfiguration`, 성공/실패 핸들러, `RefreshCookieWriter`)을 묶고 `auth-jwt-spring-boot-starter`를 의존합니다. |
| `samples/*` | `sample-jwt-api`, `sample-session-web`, `sample-hybrid-sso`의 데모 앱 |
| `docs` | 문서 소스와 멀티 모듈 안내 |

## 🧪 샘플 앱

- `sample-jwt-api`: `/api/login`으로 JWT를 발급하고 `auth-hybrid-spring-boot-starter`가 제공하는 `RefreshCookieWriter`로 쿠키를 내려줍니다. `/api/profile`로 로그인 정보를 확인할 수 있습니다.
- `sample-session-web`: `/session/login`, `/session/logout`, `/session/me`를 통해 `auth-session` SPI/필터의 흐름을 확인할 수 있습니다.
- `sample-hybrid-sso`: OAuth2 클라이언트를 설정하면 `/hybrid/me`가 성공 핸들러가 만든 `Principal`을 보여줍니다.

`auth-session`과 `auth-hybrid`는 이번 단계에서는 의존 뼈대만 확보하고 실제 구현을 확장할 수 있도록 간결하게 유지됩니다.

---

## 🚀 시작하기

### 1️⃣ Maven Central 사용

```gradle
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.jho951:auth-core:2.0.5")
    implementation("io.github.jho951:auth-jwt:2.0.5")
    implementation("io.github.jho951:auth-spring:2.0.5")
    implementation("io.github.jho951:auth-jwt-spring-boot-starter:2.0.5")
}
```

### 2️⃣ 스타터 선택

- `auth-jwt-spring-boot-starter`은 일반적인 JWT 시나리오에 필요한 `AuthAutoConfiguration`, `AuthSecurityAutoConfiguration`, `AuthJwtProperties`를 제공합니다. 이 스타터는 `auth-core`, `auth-jwt`, `auth-spring`을 기반으로 동작합니다.
- 세션 기반 인증을 준비 중이라면 `auth-session-spring-boot-starter`를 추가하고 `auth-session` SPI를 구현하면 됩니다. 현재 버전에서는 `SimpleSessionStore` 같은 얇은 기본 구현만 제공하므로, 필요하다면 자체 구현으로 대체할 수 있습니다.
- OAuth2 + JWT 하이브리드 경로를 사용하려면 `auth-hybrid-spring-boot-starter`를 사용하세요. `AuthOAuth2AutoConfiguration`, 성공/실패 핸들러, 쿠키/토큰 연결 로직을 묶어서 제공합니다.

`auth-session`과 `auth-hybrid` 모듈은 이번 릴리즈에서 최소한의 인터페이스만 노출하며, 향후 구체 구현을 이곳에 추가할 계획입니다.
---

### 3️⃣ 공통 유틸 사용
> `Strings` 같은 유틸리티는 이제 `auth-core`에 머물러 있고, 어디서든 그대로 import 해서 사용하면 됩니다.

```java
import com.auth.common.utils.Strings;

if (Strings.isBlank(username)) throw new IllegalArgumentException("username must not be blank");

String userId = Strings.requireNonBlank(rawUserId, "userId");
TokenService tokenService = Strings.requireNonNull(customTokenService, "tokenService");
```

---

### 4️⃣ application.yml 설정
- `auth-jwt-spring-boot-starter`가 `auth.jwt.secret`을 감지하면 기본 `TokenService`, `RefreshTokenExtractor`, `BCryptPasswordVerifier`, `RefreshTokenStore`(auth-common-test의 InMemory) 등을 자동 등록합니다. 쿠키 기반 refresh가 필요하면 동시에 `auth-hybrid-spring-boot-starter`를 추가하여 `RefreshCookieWriter`도 함께 등록하세요.

```yml
auth:
  refresh-cookie-name: "ADMIN_REFRESH_TOKEN"

  jwt:
    secret: ${AUTH_JWT_SECRET}
    access-seconds: 3600
    refresh-seconds: 1209600
```

`auth.jwt.refresh-seconds`는 다음 3곳에 동일하게 적용됩니다.
- Refresh JWT 만료 시간
- 서버 저장소의 Refresh Token TTL (`expiresAt`)
- Refresh 쿠키 `Max-Age`

### 5️⃣ UserFinder 구현 (필수)
> 각 서비스마다 사용자 저장 방식이 다르기 때문에 UserFinder는 반드시 애플리케이션에서 구현해야 합니다.
```java
// 예시
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

`auth-jwt-spring-boot-starter`는 구성 계층이고, JWT 기반 `TokenService`, `PasswordVerifier`, `RefreshTokenStore` 구현은 `auth-jwt`/`auth-core`에서 제공됩니다.

운영 환경에서 중앙 Redis를 사용한다면, Redis 연결과 `RefreshTokenStore` 구현은 인증 서버 애플리케이션이 직접 제공하는 것을 권장합니다.

### 6️⃣ 애플리케이션에서 API 구성
- 이 모듈은 기본 로그인/재발급/로그아웃 컨트롤러를 제공하지 않습니다.
- 서비스 애플리케이션이 `AuthService`와 `RefreshTokenExtractor`로 API를 구성하며, 쿠키 기반 refresh가 필요할 때는 `auth-hybrid-spring-boot-starter`를 함께 도입해 `RefreshCookieWriter`를 활용합니다.

### 7️⃣ OAuth2/OIDC와 함께 사용
> Google/GitHub/Kakao 같은 Provider 설정은 각 서비스 애플리케이션에서 처리하고, 인증이 끝난 내부 사용자에게 이 모듈이 JWT를 발급하도록 연결합니다.

```java
Principal principal = new Principal(user.getId(), user.getRoles());
Tokens tokens = authService.login(principal);
```

> `Principal`이 노출하는 `getAuthorities()`/`getAttributes()`는 권한 선택지에 대한 메타데이터일 뿐이며, 실제 `roles`/`scopes` 판단은 downstream 구성(예: Spring Security의 `GrantedAuthority`)에서 처리해야 합니다.

`auth-hybrid-spring-boot-starter`에는 `spring-boot-starter-oauth2-client`가 포함되어 있으며 `OAuth2PrincipalResolver`를 제공하면,
이 모듈은 OAuth2 로그인 성공 후 `{"accessToken":"..."}` JSON 응답과 refresh cookie 작성까지 자동 처리합니다.

### 8️⃣ 순수 Java 사용
> 이 모듈은 Spring Boot 없이도 사용할 수 있습니다.

```java
TokenService tokenService = new JwtTokenService(secret, 3600, 1209600);
RefreshTokenStore refreshTokenStore = new InMemoryRefreshTokenStore();
PasswordVerifier passwordVerifier = new BCryptPasswordVerifier();

AuthService authService = new AuthService(
    userFinder,
    passwordVerifier,
    tokenService,
    refreshTokenStore,
    Duration.ofDays(14)
);
```




## 🔐 GitHub Actions Environment
> 배포(`publish`) 시에만 Central Portal 인증/서명 정보가 필요합니다.

- `MAVEN_CENTRAL_USERNAME` - Central Portal user token username
- `MAVEN_CENTRAL_PASSWORD` - Central Portal user token password
- `MAVEN_CENTRAL_GPG_PRIVATE_KEY`
- `MAVEN_CENTRAL_GPG_PASSPHRASE`
- `MAVEN_CENTRAL_NAMESPACE` (예: `io.github.jho951`, 자동 publish 시 필요)

---

## 🛠 Build & Test
>프로젝트 빌드 및 테스트는 다음 명령어로 실행할 수 있습니다.

```bash
./gradlew clean build
```
---

### 🔐 Security Integration
> AuthOncePerRequestFilter가 자동으로 빈으로 등록됩니다.

```java
@Bean
SecurityFilterChain filterChain(HttpSecurity http,
		AuthOncePerRequestFilter authFilter) throws Exception {
	return http
		.csrf(csrf -> csrf.disable())
		.authorizeHttpRequests(auth -> auth
			.requestMatchers("/login", "/refresh", "/logout").permitAll()
			.anyRequest().authenticated()
		)
		.addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class)
		.build();
}
```

## 🏷 Release Policy
>릴리즈는 명확한 책임 분리를 원칙으로 합니다.

* 버전은 루트 `build.gradle`의 `version`에서 관리합니다.
* 태그(`v1.1.4`)는 직접 생성합니다. ***(현재 `v1.1.4`)***
* CI는 태그가 `push` 될 때 `publish`를 수행하고, Central Portal에 자동 게시합니다.

### 릴리즈 절차
```bash
git add -A                            
git commit -m "release: v1.1.4"
git tag -a v1.1.4 -m "release: v1.1.4"
git push origin main           
git push origin v1.1.4
```

## 📄 License
> [MIT LICENSE](./License)
