# Google OAuth2 Quickstart

이 문서는 `auth-starter` 를 사용하는 서비스 서버에서 Google 로그인과 JWT 발급을 연결하는 최소 예시입니다.

전제:

- 서비스 서버는 Spring Boot 3.x
- `auth-starter` 를 의존성으로 사용
- Google OAuth Client를 이미 발급받음

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
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope:
              - openid
              - profile
              - email
```

기본 로그인 시작 URL:

```text
GET /oauth2/authorization/google
```

기본 callback URL:

```text
GET /login/oauth2/code/google
```

Google Console의 승인된 리디렉션 URI도 같은 값으로 맞춰야 합니다.

예:

```text
https://api.example.com/login/oauth2/code/google
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
public class DefaultOAuth2PrincipalResolver implements OAuth2PrincipalResolver {

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;

    public DefaultOAuth2PrincipalResolver(
        UserRepository userRepository,
        SocialAccountRepository socialAccountRepository
    ) {
        this.userRepository = userRepository;
        this.socialAccountRepository = socialAccountRepository;
    }

    @Override
    public Principal resolve(OAuth2UserIdentity identity) {
        SocialAccount socialAccount = socialAccountRepository
            .findByProviderAndProviderUserId(identity.getProvider(), identity.getProviderUserId())
            .orElseGet(() -> register(identity));

        User user = socialAccount.getUser();
        return new Principal(String.valueOf(user.getId()), user.getRoles());
    }

    private SocialAccount register(OAuth2UserIdentity identity) {
        User user = userRepository.findByEmail(identity.getEmail())
            .orElseGet(() -> userRepository.save(User.createGoogleUser(identity.getEmail(), identity.getName())));

        return socialAccountRepository.save(
            SocialAccount.of("google", identity.getProviderUserId(), user)
        );
    }
}
```

핵심은 다음 두 가지입니다.

- `identity.getProvider()` 는 보통 `google`
- `identity.getProviderUserId()` 는 Google의 고유 사용자 식별자

## 4. 로그인 성공 응답

설정이 정상이고 `OAuth2PrincipalResolver` 가 등록되어 있으면, Google 로그인 성공 후 이 모듈이 자동으로 다음 응답을 만듭니다.

```http
200 OK
Set-Cookie: ADMIN_REFRESH_TOKEN=...; HttpOnly; Secure; Path=/; SameSite=Lax
Content-Type: application/json

{"accessToken":"..."}
```

즉 서비스 서버가 별도 success handler를 만들지 않아도 됩니다.

## 5. 프론트엔드 연동 방식

브라우저 기반 로그인:

- 프론트엔드가 `/oauth2/authorization/google` 로 이동
- Google 로그인 완료 후 백엔드 callback 도착
- 백엔드가 JSON + refresh cookie 응답 반환

주의:

- 이 기본 동작은 `redirect` 가 아니라 JSON 응답입니다.
- 프론트엔드에서 최종 리디렉션 URL이 꼭 필요하면, 서비스 서버에서 success handler를 직접 등록해 교체해야 합니다.

## 6. SecurityFilterChain을 직접 쓰는 경우

서비스 서버가 이미 `SecurityFilterChain` 빈을 가지고 있으면 이 모듈의 기본 보안 자동 구성은 적용되지 않을 수 있습니다.

그 경우 최소한 아래는 직접 포함해야 합니다.

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
- `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` 는 시크릿으로 관리
- 운영에서는 `auth.refresh-cookie-secure=true`
- 프론트와 백엔드가 다른 도메인이면 `SameSite`, CORS, 쿠키 정책 추가 점검 필요
- 계정 자동 연결 정책은 이메일만 믿지 말고 서비스 정책에 맞게 결정

## 8. 트러블슈팅 포인트

- `401 OAUTH2_AUTHENTICATION_FAILED`
  - Google client 설정 오류
  - redirect URI 불일치
  - `OAuth2PrincipalResolver` 내부 예외

- 로그인 후 `/oauth2/authorization/google` 만 반복
  - 서비스 서버의 사용자 정의 `SecurityFilterChain` 이 success handler를 연결하지 않았을 가능성

- access token은 오는데 refresh cookie가 없음
  - `auth.refresh-cookie-enabled=false`
  - `Secure` 쿠키가 HTTP 환경에서 무시됨
