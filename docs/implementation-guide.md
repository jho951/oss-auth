# 구현 가이드

핵심 원칙:

- 이 저장소는 credential을 검증하고 `Principal`을 반환합니다.
- HTTP request, filter, controller, gateway route, 조직 header 해석은 이 저장소 밖에서 처리합니다.
- URL별 허용 정책, browser/internal/external boundary 판단, role 기반 인가는 상위 계층 책임입니다.

예시 코드는 핵심 조립 방식만 보여줍니다.
repository, hasher, HTTP request adapter, import 문은 실행 계층에서 준비합니다.

## 1. 의존성 선택

권장 방식은 BOM을 먼저 가져오고 필요한 capability만 추가하는 것입니다.

```gradle
dependencies {
    implementation(platform("io.github.jho951:auth-bom:<version>"))

    implementation("io.github.jho951:auth-core")
    implementation("io.github.jho951:auth-jwt")
    implementation("io.github.jho951:auth-session")
    implementation("io.github.jho951:auth-apikey")
    implementation("io.github.jho951:auth-hmac")
    implementation("io.github.jho951:auth-dpop")
    implementation("io.github.jho951:auth-mfa")
    implementation("io.github.jho951:auth-mtls")
    implementation("io.github.jho951:auth-oidc")
    implementation("io.github.jho951:auth-otp")
    implementation("io.github.jho951:auth-saml")
    implementation("io.github.jho951:auth-service-account")
    implementation("io.github.jho951:auth-webauthn")
}
```

버전을 개별로 고정해야 하는 환경이라면 각 모듈에 `<version>`을 직접 적어도 됩니다.

### 선택 기준

| 필요한 기능                         | 추가할 모듈                 |
|--------------------------------|------------------------|
| 공식 지원 버전 세트 사용               | `auth-bom`             |
| username/password login, 공통 모델 | `auth-core`            |
| JWT 발급/검증                      | `auth-jwt`             |
| session 발급/조회                  | `auth-session`         |
| API key 인증                     | `auth-apikey`          |
| HMAC request signature 인증      | `auth-hmac`            |
| DPoP proof 검증                 | `auth-dpop`            |
| MFA step-up 정책과 factor 검증    | `auth-mfa`             |
| mTLS client certificate 인증     | `auth-mtls`            |
| OIDC ID token 인증               | `auth-oidc`            |
| HOTP/TOTP/recovery code 검증     | `auth-otp`             |
| SAML assertion 검증              | `auth-saml`            |
| service account 인증             | `auth-service-account` |
| WebAuthn/passkey assertion/attestation | `auth-webauthn` |

## 2. Principal 모델

`Principal`은 인증 결과의 최소 단위입니다.

```java
Principal principal = new Principal(
    "user-1",
    Arrays.asList("USER"),
    Collections.singletonMap("tenant_id", "tenant-a")
);
```

### 주의:

- `userId`는 인증된 주체의 안정적인 식별자입니다.
- `authorities`는 범용 권한 문자열입니다.
- `attributes`는 인증 결과에 필요한 부가 정보입니다.
- 문서 수정 가능 여부, 주문 승인 가능 여부 같은 도메인 권한 판단은 넣지 않습니다.

주체 유형을 명확히 표현해야 하면 `AuthenticatedSubject`를 사용합니다.

```java
AuthenticatedSubject service = ServicePrincipal.of(
    "billing-worker",
    AuthoritySet.of(Arrays.asList("SERVICE")),
    Collections.singletonMap("credential_type", "service_account")
);

Principal principal = service.toPrincipal();
```

## 3. Username/Password Login

`AuthService`는 username/password 검증, access/refresh token 발급, refresh token 저장을 조립합니다.

```java
UserFinder userFinder = username -> userRepository.findByUsername(username)
    .map(row -> new User(
        row.id(),
        row.username(),
        row.passwordHash(),
        row.authorities()
    ));

PasswordVerifier passwordVerifier = (rawPassword, passwordHash) ->
    passwordHasher.matches(rawPassword, passwordHash);

UserStatusChecker userStatusChecker = user ->
    user.getAuthorities().isEmpty()
        ? Optional.of(AuthFailureReason.USER_DISABLED)
        : Optional.empty();

TokenService tokenService = new JwtTokenService(
    "01234567890123456789012345678901",
    900,
    1_209_600
);

RefreshTokenStore refreshTokenStore = new DatabaseRefreshTokenStore(dataSource);

AuthService authService = new AuthService(
    userFinder,
    passwordVerifier,
    userStatusChecker,
    tokenService,
    refreshTokenStore,
    Duration.ofDays(14)
);

Tokens tokens = authService.login("user@example.com", "raw-password");
```

구현해야 하는 것:

- `UserFinder`: 사용자 저장소 조회
- `PasswordVerifier`: password hash 검증
- `UserStatusChecker`: 사용자 인증 가능 상태 판정
- `RefreshTokenStore`: refresh token 저장/조회/폐기

1계층 밖에서 결정해야 하는 것:

- `/auth/login` URL
- request body parsing
- cookie/header 발급 방식
- 실패 응답 JSON 형식
- rate limit, account lock, captcha

## 4. JWT 기본 구현

HS256 기반 기본 발급/검증:

```java
JwtTokenService tokenService = new JwtTokenService(
    "01234567890123456789012345678901",
    900,
    1_209_600
);

String accessToken = tokenService.issueAccessToken(principal);
Principal verified = tokenService.verifyAccessToken(accessToken);
```

주의:

- secret은 HS256 기준 최소 32 bytes가 필요합니다.
- access token과 refresh token은 `token_type` claim으로 구분됩니다.
- JWT 권한 정보는 `authorities` claim 하나만 사용합니다.

## 5. JWT Key Rotation

`JwtSigningKeyProvider`는 발급 key를 제공합니다.
`JwtKeyResolver`는 검증 시 `kid`로 key를 선택합니다.

```java
Key oldKey = Keys.hmacShaKeyFor(oldSecret.getBytes(StandardCharsets.UTF_8));
Key newKey = Keys.hmacShaKeyFor(newSecret.getBytes(StandardCharsets.UTF_8));

JwtSigningKeyProvider signingKeyProvider =
    new StaticJwtSigningKeyProvider(newKey, "2026-04");

JwtKeyResolver keyResolver =
    new StaticJwtKeyResolver(oldKey, Collections.singletonMap("2026-04", newKey));

JwtTokenService tokenService = new JwtTokenService(
    signingKeyProvider,
    keyResolver,
    new DefaultJwtClaimsMapper(),
    new JwtTokenOptions(
        Duration.ofMinutes(15),
        Duration.ofDays(14),
        SignatureAlgorithm.HS256,
        null
    )
);
```

운영 구현에서는 보통 `JwtKeyResolver`를 다음 중 하나로 바꿉니다.

- JWKS 기반 resolver
- KMS/HSM 기반 resolver
- DB 또는 secret manager 기반 resolver
- in-memory cache를 가진 remote key resolver

경계:

- key 선택과 서명 검증은 1계층입니다.
- 특정 서비스 issuer/audience 강제값은 상위 정책 계층에서 주입하거나 claim mapper 설정으로 분리합니다.

## 6. JWT Claim Mapping

기본 claim schema가 부족하면 `JwtClaimsMapper`를 교체합니다.

```java
public final class TenantJwtClaimsMapper implements JwtClaimsMapper {
    @Override
    public Map<String, Object> toClaims(Principal principal) {
        Map<String, Object> claims = new HashMap<>(principal.getAttributes());
        claims.put("authorities", principal.getAuthorities());
        claims.put("tenant_id", principal.getAttribute("tenant_id"));
        return claims;
    }

    @Override
    public Principal toPrincipal(Claims claims, String expectedType) {
        String type = claims.get("token_type", String.class);
        if (!expectedType.equals(type)) {
            throw new AuthException(AuthFailureReason.INVALID_TOKEN, "invalid token type");
        }

        List<String> authorities = claims.get("authorities", List.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("tenant_id", claims.get("tenant_id"));
        return new Principal(claims.getSubject(), authorities, attributes);
    }
}
```

주의:

- mapper는 claim과 `Principal` 변환만 담당합니다.
- 특정 endpoint에서 어떤 claim을 요구할지는 상위 계층이 판단합니다.

## 7. Refresh Token Store

`RefreshTokenStore`는 서버 기준 refresh token 상태를 관리합니다.

```java
public final class DatabaseRefreshTokenStore implements RefreshTokenStore {
    @Override
    public void save(String userId, String refreshToken, Instant expiresAt) {
        // insert or upsert refresh token
    }

    @Override
    public boolean exists(String userId, String refreshToken) {
        // return true only when token exists and is not expired/revoked
        return false;
    }

    @Override
    public void revoke(String userId, String refreshToken) {
        // delete or mark revoked
    }
}
```

운영 구현 체크리스트:

- token 원문 저장 대신 hash 저장을 고려합니다.
- 만료된 token은 조회에서 실패해야 합니다.
- 여러 인스턴스에서 같은 저장소를 사용해야 합니다.
- rotation 시 이전 refresh token은 즉시 폐기되어야 합니다.

## 8. Session 발급과 조회

이 모듈은 운영용 `SessionStore` 구현을 직접 제공하지 않습니다.
세션 저장소는 애플리케이션이나 2계층에서 주입합니다.

```java
SessionStore sessionStore = new RedisSessionStore(sessionRepository);
SessionIdGenerator idGenerator = new SecureRandomSessionIdGenerator();

SessionService sessionService = new SessionService(
    sessionStore,
    idGenerator,
    Duration.ofHours(1)
);

String sessionId = sessionService.create(principal);
Optional<Principal> resolved = sessionService.resolve(sessionId);
```

기본 `SecureRandomSessionIdGenerator()`는 24바이트 난수를 사용합니다.
길이 정책을 상위 계층에서 바꿔야 하면 생성자를 통해 다른 바이트 수를 줄 수 있습니다.

```java
SessionIdGenerator idGenerator = new SecureRandomSessionIdGenerator(16);
```

session 인증 provider:

```java
SessionAuthenticationProvider sessionProvider =
    new DefaultSessionAuthenticationProvider(
        sessionStore,
        new IdentitySessionPrincipalMapper()
    );

Optional<Principal> authenticated = sessionProvider.authenticate(sessionId);
```

`SessionStore` 또는 `SessionRecordStore`는 Redis/JDBC 같은 외부 저장소 구현으로 준비합니다.

## 9. Sliding Session Expiration

session metadata와 sliding expiration이 필요하면 `SessionRecordStore`와 decorator를 사용합니다.

```java
SessionRecordStore baseStore = new RedisSessionRecordStore(sessionRepository);

SessionRecordStore slidingStore = new SlidingExpirationSessionStoreDecorator(
    baseStore,
    new SlidingSessionExpirationPolicy(Duration.ofMinutes(30))
);

slidingStore.save("session-1", principal);
Optional<Principal> resolved = slidingStore.find("session-1");
```

경계:

- fixed/sliding expiration 계산은 1계층입니다.
- “admin은 session 금지”, “browser는 session 우선” 같은 정책은 1계층이 아닙니다.

## 10. 상위 계층에서 JWT와 session을 조합하는 예시

JWT와 session 조합은 이 저장소의 별도 모듈이 아니라 상위 계층에서 결정합니다.

브라우저 요청에서 bearer token이 없거나 정책상 fallback을 허용하는 예시:

```java
Optional<Principal> authenticateBrowserRequest(RequestLike request) {
    if (request.bearerToken().isPresent()) {
        try {
            return Optional.of(tokenService.verifyAccessToken(request.bearerToken().get()));
        } catch (AuthException ex) {
            // browser boundary 정책에 따라 session fallback 허용
        }
    }

    return request.sessionId().flatMap(sessionProvider::authenticate);
}
```

외부 API처럼 bearer만 허용하는 경계의 예시:

```java
Optional<Principal> authenticateExternalApi(RequestLike request) {
    if (!request.bearerToken().isPresent()) {
        return Optional.empty();
    }

    return Optional.of(tokenService.verifyAccessToken(request.bearerToken().get()));
}
```

경계:

- `auth-jwt`와 `auth-session`은 각각 token 검증과 session 조회만 담당합니다.
- source priority, fallback, invalid token hard-fail 여부, boundary별 허용 source는 상위 계층 정책입니다.

## 11. API Key 인증

API key 검증은 `ApiKeyPrincipalResolver`에 위임합니다.

```java
ApiKeyPrincipalResolver resolver = credential -> apiKeyRepository
    .findActiveByKeyId(credential.keyId())
    .filter(row -> apiKeyHasher.matches(credential.secret(), row.secretHash()))
    .map(row -> new Principal(
        row.principalId(),
        row.authorities(),
        Collections.singletonMap("credential_type", "api_key")
    ));

ApiKeyAuthenticationProvider provider =
    new ApiKeyAuthenticationProvider(resolver);

Optional<Principal> principal =
    provider.authenticate(new ApiKeyCredential("key-id", "raw-api-key"));
```

경계:

- API key 검증과 principal 변환은 1계층입니다.
- `X-Api-Key` 같은 header 이름은 adapter/상위 계층 책임입니다.

## 12. HMAC 인증

HMAC provider는 secret 조회, signature 검증, principal 조회를 조합합니다.

```java
HmacSecretResolver secretResolver = keyId ->
    keyRepository.findActiveSecret(keyId);

HmacSignatureVerifier signatureVerifier = (request, secret) ->
    hmacVerifier.verifyCanonicalRequest(request, secret);

HmacPrincipalResolver principalResolver = keyId ->
    keyRepository.findPrincipal(keyId);

HmacAuthenticationProvider provider = new HmacAuthenticationProvider(
    secretResolver,
    signatureVerifier,
    principalResolver
);

HmacAuthenticationRequest request = new HmacAuthenticationRequest(
    "key-id",
    "POST",
    "/documents",
    bodyBytes,
    Collections.singletonMap("content-type", "application/json"),
    signature,
    Instant.now()
);

Optional<Principal> principal = provider.authenticate(request);
```

경계:

- canonical request 검증은 1계층입니다.
- 어떤 HTTP header를 signature에 포함할지 강제하는 프로젝트 규약은 상위 계층 설정입니다.

## 13. OIDC 인증

OIDC는 ID token 검증과 principal mapping을 분리합니다.

```java
OidcTokenVerifier tokenVerifier = request ->
    oidcLibrary.verifyIdToken(request.idToken(), request.nonce());

OidcPrincipalMapper principalMapper = identity ->
    {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("issuer", identity.issuer());
        attributes.put("audience", identity.audience());
        return new Principal(identity.subject(), Arrays.asList("USER"), attributes);
    };

OidcAuthenticationProvider provider =
    new OidcAuthenticationProvider(tokenVerifier, principalMapper);

Optional<Principal> principal =
    provider.authenticate(new OidcAuthenticationRequest(idToken, nonce));
```

경계:

- ID token 서명/만료/nonce 검증은 1계층 capability입니다.
- 특정 회사 email domain만 허용하거나 IdP group을 admin으로 매핑하는 것은 상위 계층 정책입니다.

## 14. MFA Step-up

MFA는 1차 인증이 끝난 `Principal`에 대해 "지금 추가 인증이 필요한가"를 판단하고, 선택된 factor 검증이 성공하면 `Principal`을 승격합니다.

```java
MfaEnrollmentResolver enrollmentResolver = principal -> mfaRepository
    .findActiveByUserId(principal.getUserId())
    .stream()
    .map(row -> new MfaEnrollment(
        row.factorId(),
        row.factorType(),
        row.enrolledAt(),
        Collections.singletonMap("label", row.label())
    ))
    .collect(Collectors.toList());

MfaPolicy mfaPolicy = new ActionOrRiskBasedMfaPolicy(
    Arrays.asList("wire-transfer", "change-password"),
    MfaRiskLevel.HIGH,
    Arrays.asList(MfaFactorType.PASSKEY, MfaFactorType.TOTP),
    null
);

MfaVerifier totpVerifier = new TotpMfaVerifier(new TotpVerifier());

MfaVerifier passkeyVerifier = new MfaVerifier() {
    @Override
    public boolean supports(MfaFactorType factorType) {
        return factorType == MfaFactorType.PASSKEY;
    }

    @Override
    public Optional<Map<String, Object>> verify(MfaVerificationRequest request, MfaEnrollment enrollment) {
        return passkeyLibrary.verify(request.getProof(), enrollment.getAttributes())
            ? Optional.of(Collections.singletonMap("verified_by", "passkey"))
            : Optional.empty();
    }
};

MfaService mfaService = new MfaService(
    enrollmentResolver,
    mfaPolicy,
    Arrays.asList(totpVerifier, passkeyVerifier),
    new DefaultMfaPrincipalMapper()
);

MfaChallengeContext context = new MfaChallengeContext(
    "wire-transfer",
    MfaRiskLevel.HIGH,
    Collections.singletonMap("ip", "203.0.113.10")
);

MfaRequirement requirement = mfaService.evaluate(principal, context);

Optional<Principal> elevated = mfaService.stepUp(new MfaVerificationRequest(
    principal,
    "totp-1",
    MfaFactorType.TOTP,
    context,
    Collections.singletonMap("code", "123456")
));
```

경계:

- `auth-mfa`는 requirement 평가와 factor 검증 조립까지만 담당합니다.
- risk signal 수집, OTP 발송, challenge 화면, 시도 횟수 제한, WebAuthn ceremony는 상위 계층 책임입니다.

`auth-mfa`에는 OTP 기반 기본 verifier도 함께 들어 있습니다.

```java
TotpVerifier totpVerifier = new TotpVerifier();
MfaVerifier totpMfaVerifier = new TotpMfaVerifier(totpVerifier);

Sha256RecoveryCodeVerifier recoveryCodeVerifier = new Sha256RecoveryCodeVerifier();
MfaVerifier recoveryMfaVerifier = new RecoveryCodeMfaVerifier(recoveryCodeVerifier);

MfaService mfaService = new MfaService(
    enrollmentResolver,
    mfaPolicy,
    Arrays.asList(totpMfaVerifier, recoveryMfaVerifier),
    new DefaultMfaPrincipalMapper()
);
```

기본 약속:

- `TotpMfaVerifier`는 enrollment attribute의 `otp_secret` 값을 사용합니다.
- `RecoveryCodeMfaVerifier`는 enrollment attribute의 `recovery_hashes` 목록을 사용합니다.
- 두 verifier 모두 request proof의 `code` 값을 읽습니다.
- 다른 capability와 공통으로 맞출 key는 [소비 규약](./consumption-conventions.md)을 기준으로 정합니다.

## 15. Service Account 인증

service account는 사람 사용자가 아니라 machine principal을 반환합니다.

```java
ServiceAccountVerifier verifier = credential -> serviceAccountRepository
    .findActive(credential.serviceId())
    .filter(row -> secretVerifier.matches(credential.secret(), row.secretHash()))
    .map(row -> ServicePrincipal.of(
        row.serviceId(),
        AuthoritySet.of(row.authorities()),
        Collections.singletonMap("credential_type", "service_account")
    ).toPrincipal());

ServiceAccountAuthenticationProvider provider =
    new ServiceAccountAuthenticationProvider(verifier);

Optional<Principal> principal = provider.authenticate(
    new ServiceAccountCredential("billing-worker", "raw-secret")
);
```

경계:

- service credential 검증은 1계층입니다.
- “internal API만 service account 허용”은 상위 계층 정책입니다.

## 16. 상위 계층에서 조립하는 방식

상위 계층은 request에서 credential을 꺼낸 뒤 1계층 provider에 넘기고, 조합 순서와 fallback 정책도 같이 결정합니다.

```java
Optional<Principal> authenticate(RequestLike request) {
    if (request.bearerToken().isPresent()) {
        return Optional.of(tokenService.verifyAccessToken(request.bearerToken().get()));
    }

    if (request.sessionId().isPresent()) {
        return sessionProvider.authenticate(request.sessionId().get());
    }

    if (request.apiKey().isPresent()) {
        return apiKeyProvider.authenticate(request.apiKey().get());
    }

    return Optional.empty();
}
```

이 코드는 예시일 뿐이며, 실제 위치는 이 저장소가 아닙니다.
상위 계층에서 URL, method, boundary, header, cookie, error response를 결정합니다.

## 17. 구현 전 체크리스트

- 이 구현이 특정 URL을 알고 있는가?
- 이 구현이 특정 framework request type을 받고 있는가?
- 이 구현이 특정 조직 header 이름을 알고 있는가?
- 이 구현이 gateway/internal/external boundary를 판단하는가?
- 이 구현이 도메인 권한을 판단하는가?

하나라도 맞으면 1계층 구현이 아닙니다.

1계층 구현은 다음 형태로 끝나야 합니다.

```text
credential -> verify -> Principal
token -> verify -> Principal
sessionId -> lookup -> Principal
Principal -> issue token/session
```
