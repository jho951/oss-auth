# GitHub OAuth2 Quickstart

이 문서는 `auth-starter` 를 사용하는 서비스 서버에서 GitHub 로그인과 JWT 발급을 연결하는 최소 예시입니다.

전제:

- 서비스 서버는 Spring Boot 3.x
- `auth-starter` 를 의존성으로 사용
- GitHub OAuth App 또는 GitHub App 기반 OAuth Client를 이미 발급받음

## 1. 의존성

서비스 서버 `build.gradle` 예시:

```gradle
dependencies {
    implementation("io.github.jho951:auth-starter:1.1.3")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
}
```

## 2. application.yml

```yaml
auth:
  refresh-cookie-name: "ADMIN_REFRESH_TOKEN"
  refresh-cookie-secure: true
  oauth2:
    enabled: true
  jwt:
    secret: ${AUTH_JWT_SECRET}
    access-seconds: 3600
    refresh-seconds: 1209600

spring:
  security:
    oauth2:
      client:
        registration:
          github:
            client-id: ${GITHUB_CLIENT_ID}
            client-secret: ${GITHUB_CLIENT_SECRET}
            scope:
              - read:user
              - user:email
```

기본 로그인 시작 URL:

```text
GET /oauth2/authorization/github
```

기본 callback URL:

```text
GET /login/oauth2/code/github
```

GitHub OAuth App의 Authorization callback URL 도 같은 값으로 맞춰야 합니다.

예:

```text
https://api.example.com/login/oauth2/code/github
```

## 3. 내부 사용자 매핑

서비스 서버는 반드시 `OAuth2PrincipalResolver` 를 구현해야 합니다.

```java
package com.example.auth;

import org.springframework.stereotype.Component;

import com.auth.api.model.OAuth2UserIdentity;
import com.auth.api.model.Principal;
import com.auth.spi.OAuth2PrincipalResolver;

@Component
public class GithubOAuth2PrincipalResolver implements OAuth2PrincipalResolver {

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;

    public GithubOAuth2PrincipalResolver(
        UserRepository userRepository,
        SocialAccountRepository socialAccountRepository
    ) {
        this.userRepository = userRepository;
        this.socialAccountRepository = socialAccountRepository;
    }

    @Override
    public Principal resolve(OAuth2UserIdentity identity) {
        String githubUserId = identity.getProviderUserId();
        String email = resolveEmail(identity);
        String username = readString(identity.getAttributes(), "login");

        SocialAccount socialAccount = socialAccountRepository
            .findByProviderAndProviderUserId(identity.getProvider(), githubUserId)
            .orElseGet(() -> register(identity.getProvider(), githubUserId, email, username));

        User user = socialAccount.getUser();
        return new Principal(String.valueOf(user.getId()), user.getRoles());
    }

    private SocialAccount register(String provider, String providerUserId, String email, String username) {
        User user = userRepository.findByEmail(email)
            .orElseGet(() -> userRepository.save(User.createGithubUser(email, username)));

        return socialAccountRepository.save(
            SocialAccount.of(provider, providerUserId, user)
        );
    }

    private String resolveEmail(OAuth2UserIdentity identity) {
        if (identity.getEmail() != null && !identity.getEmail().isBlank()) {
            return identity.getEmail();
        }
        throw new IllegalStateException("GitHub email is missing. Check scope or fallback email lookup policy.");
    }

    private String readString(java.util.Map<String, Object> attributes, String key) {
        Object value = attributes.get(key);
        return value == null ? null : String.valueOf(value);
    }
}
```

핵심은 다음입니다.

- `identity.getProvider()` 는 보통 `github`
- `identity.getProviderUserId()` 는 GitHub 사용자 `id`
- `identity.getEmail()` 은 항상 온다고 가정하면 안 됨

## 4. 로그인 성공 응답

설정이 정상이고 `OAuth2PrincipalResolver` 가 등록되어 있으면, GitHub 로그인 성공 후 이 모듈이 자동으로 다음 응답을 만듭니다.

```http
200 OK
Set-Cookie: ADMIN_REFRESH_TOKEN=...; HttpOnly; Secure; Path=/; SameSite=Lax
Content-Type: application/json

{"accessToken":"..."}
```

## 5. 프론트엔드 연동 방식

브라우저 기반 로그인:

- 프론트엔드가 `/oauth2/authorization/github` 로 이동
- GitHub 로그인 완료 후 백엔드 callback 도착
- 백엔드가 JSON + refresh cookie 응답 반환

주의:

- 기본 동작은 redirect가 아니라 JSON 응답입니다.
- 프론트엔드 최종 이동 URL이 필요하면 success handler를 교체해야 합니다.

## 6. SecurityFilterChain을 직접 쓰는 경우

서비스 서버가 이미 `SecurityFilterChain` 빈을 가지고 있으면 아래처럼 handler를 직접 연결해야 합니다.

```java
package com.example.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.auth.config.AuthProperties;
import com.auth.config.security.AuthOncePerRequestFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(
        HttpSecurity http,
        AuthOncePerRequestFilter authFilter,
        AuthProperties authProperties,
        @Qualifier("authOAuth2AuthenticationSuccessHandler") AuthenticationSuccessHandler successHandler,
        @Qualifier("authOAuth2AuthenticationFailureHandler") AuthenticationFailureHandler failureHandler
    ) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers(
                    authProperties.getOauth2().getAuthorizationBaseUri() + "/**",
                    authProperties.getOauth2().getLoginProcessingBaseUri()
                ).permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(endpoint ->
                    endpoint.baseUri(authProperties.getOauth2().getAuthorizationBaseUri())
                )
                .redirectionEndpoint(endpoint ->
                    endpoint.baseUri(authProperties.getOauth2().getLoginProcessingBaseUri())
                )
                .successHandler(successHandler)
                .failureHandler(failureHandler)
            )
            .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
```

## 7. 운영 체크리스트

- `AUTH_JWT_SECRET` 는 32바이트 이상 사용
- `GITHUB_CLIENT_ID`, `GITHUB_CLIENT_SECRET` 는 시크릿으로 관리
- `user:email` scope 누락 여부 확인
- 운영에서는 `auth.refresh-cookie-secure=true`
- 이메일이 비공개인 계정 처리 정책을 미리 정해야 함

## 8. 트러블슈팅 포인트

- `401 OAUTH2_AUTHENTICATION_FAILED`
  - GitHub client 설정 오류
  - callback URL 불일치
  - `OAuth2PrincipalResolver` 내부 예외

- 이메일이 `null`
  - `user:email` scope 누락
  - GitHub 계정 공개 이메일 없음
  - 서비스 정책상 별도 이메일 보강 API 호출 필요
