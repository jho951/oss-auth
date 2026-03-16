# Kakao OAuth2 Quickstart

이 문서는 `auth-starter` 를 사용하는 서비스 서버에서 Kakao 로그인과 JWT 발급을 연결하는 최소 예시입니다.

전제:

- 서비스 서버는 Spring Boot 3.x
- `auth-starter` 를 의존성으로 사용
- Kakao Developers 에서 REST API 키와 Client Secret 설정을 완료함

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

Kakao는 Spring Boot 기본 provider가 아니라서 `registration` 과 `provider` 를 같이 선언하는 편이 일반적입니다.

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
          kakao:
            client-id: ${KAKAO_CLIENT_ID}
            client-secret: ${KAKAO_CLIENT_SECRET}
            client-authentication-method: client_secret_post
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
            scope:
              - profile_nickname
              - account_email
            client-name: Kakao
        provider:
          kakao:
            authorization-uri: https://kauth.kakao.com/oauth/authorize
            token-uri: https://kauth.kakao.com/oauth/token
            user-info-uri: https://kapi.kakao.com/v2/user/me
            user-name-attribute: id
```

기본 로그인 시작 URL:

```text
GET /oauth2/authorization/kakao
```

기본 callback URL:

```text
GET /login/oauth2/code/kakao
```

Kakao Developers 의 Redirect URI 도 같은 값으로 맞춰야 합니다.

예:

```text
https://api.example.com/login/oauth2/code/kakao
```

## 3. 내부 사용자 매핑

Kakao는 attribute 구조가 중첩돼 있으므로 `identity.getAttributes()` 에서 직접 읽는 경우가 많습니다.

```java
package com.example.auth;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.auth.api.model.OAuth2UserIdentity;
import com.auth.api.model.Principal;
import com.auth.spi.OAuth2PrincipalResolver;

@Component
public class KakaoOAuth2PrincipalResolver implements OAuth2PrincipalResolver {

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;

    public KakaoOAuth2PrincipalResolver(
        UserRepository userRepository,
        SocialAccountRepository socialAccountRepository
    ) {
        this.userRepository = userRepository;
        this.socialAccountRepository = socialAccountRepository;
    }

    @Override
    public Principal resolve(OAuth2UserIdentity identity) {
        String provider = identity.getProvider();
        String providerUserId = identity.getProviderUserId();
        String email = resolveEmail(identity);
        String nickname = resolveNickname(identity);

        SocialAccount socialAccount = socialAccountRepository
            .findByProviderAndProviderUserId(provider, providerUserId)
            .orElseGet(() -> register(provider, providerUserId, email, nickname));

        User user = socialAccount.getUser();
        return new Principal(String.valueOf(user.getId()), user.getRoles());
    }

    private SocialAccount register(String provider, String providerUserId, String email, String nickname) {
        User user = userRepository.findByEmail(email)
            .orElseGet(() -> userRepository.save(User.createKakaoUser(email, nickname)));

        return socialAccountRepository.save(
            SocialAccount.of(provider, providerUserId, user)
        );
    }

    @SuppressWarnings("unchecked")
    private String resolveEmail(OAuth2UserIdentity identity) {
        if (identity.getEmail() != null && !identity.getEmail().isBlank()) {
            return identity.getEmail();
        }
        Map<String, Object> kakaoAccount = (Map<String, Object>) identity.getAttributes().get("kakao_account");
        if (kakaoAccount == null) {
            return null;
        }
        Object email = kakaoAccount.get("email");
        return email == null ? null : String.valueOf(email);
    }

    @SuppressWarnings("unchecked")
    private String resolveNickname(OAuth2UserIdentity identity) {
        if (identity.getName() != null && !identity.getName().isBlank()) {
            return identity.getName();
        }
        Map<String, Object> properties = (Map<String, Object>) identity.getAttributes().get("properties");
        if (properties == null) {
            return null;
        }
        Object nickname = properties.get("nickname");
        return nickname == null ? null : String.valueOf(nickname);
    }
}
```

핵심은 다음입니다.

- `identity.getProvider()` 는 보통 `kakao`
- `identity.getProviderUserId()` 는 Kakao 사용자 `id`
- 이메일과 닉네임은 `attributes` 중첩 구조에서 읽어야 할 수 있음

## 4. 로그인 성공 응답

설정이 정상이고 `OAuth2PrincipalResolver` 가 등록되어 있으면, Kakao 로그인 성공 후 이 모듈이 자동으로 다음 응답을 만듭니다.

```http
200 OK
Set-Cookie: ADMIN_REFRESH_TOKEN=...; HttpOnly; Secure; Path=/; SameSite=Lax
Content-Type: application/json

{"accessToken":"..."}
```

## 5. 프론트엔드 연동 방식

브라우저 기반 로그인:

- 프론트엔드가 `/oauth2/authorization/kakao` 로 이동
- Kakao 로그인 완료 후 백엔드 callback 도착
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
- `KAKAO_CLIENT_ID`, `KAKAO_CLIENT_SECRET` 는 시크릿으로 관리
- Kakao Developers 의 동의항목과 scope가 실제로 맞는지 확인
- 운영에서는 `auth.refresh-cookie-secure=true`
- 이메일 제공이 선택 동의일 수 있으니 누락 정책을 정해야 함

## 8. 트러블슈팅 포인트

- `401 OAUTH2_AUTHENTICATION_FAILED`
  - Kakao client 설정 오류
  - redirect URI 불일치
  - `OAuth2PrincipalResolver` 내부 예외

- 이메일이나 닉네임이 `null`
  - Kakao 동의 항목 미설정
  - `account_email`, `profile_nickname` scope 누락
  - `attributes` 중첩 구조를 제대로 읽지 않음
