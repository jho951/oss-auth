# 테스트/CI 가이드

## 로컬 테스트 실행

### 전체 빌드

```bash
./gradlew clean build
```

### 모듈 단위 테스트:

```bash
./gradlew :auth-bom:generatePomFileForMavenBomPublication
./gradlew :auth-core:test
./gradlew :auth-jwt:test
./gradlew :auth-session:test
./gradlew :auth-apikey:test
./gradlew :auth-hmac:test
./gradlew :auth-dpop:test
./gradlew :auth-mfa:test
./gradlew :auth-mtls:test
./gradlew :auth-oidc:test
./gradlew :auth-otp:test
./gradlew :auth-saml:test
./gradlew :auth-service-account:test
./gradlew :auth-webauthn:test
```

## 현재 테스트 범위

- `auth-bom`
  - BOM POM 생성 검증
- `auth-core`
  - `AuthExceptionTest`
  - `OAuth2UserIdentityTest`
  - `PrincipalTest`
  - `TokensTest`
  - `UserTest`
  - `AuthServiceTest`
  - `StringsTest`
- `auth-jwt`
  - `JwtTokenServiceTest`
- `auth-session`
  - `DefaultSessionAuthenticationProviderTest`
  - `SessionServiceTest`
- `auth-apikey`
  - `ApiKeyAuthenticationProviderTest`
- `auth-hmac`
  - 현재 provider/SPI compile 검증
- `auth-dpop`
  - `DpopProofVerifierTest`
- `auth-mfa`
  - `ActionOrRiskBasedMfaPolicyTest`
  - `MfaServiceTest`
  - `TotpMfaVerifierTest`
  - `RecoveryCodeMfaVerifierTest`
- `auth-mtls`
  - `MtlsAuthenticationProviderTest`
- `auth-oidc`
  - 현재 provider/SPI compile 검증
- `auth-otp`
  - `HotpVerifierTest`
  - `TotpVerifierTest`
  - `Sha256RecoveryCodeVerifierTest`
- `auth-saml`
  - `DefaultSamlAssertionValidatorTest`
  - `SamlAuthenticationProviderTest`
- `auth-service-account`
  - `X509ServiceAccountAuthenticationProviderTest`
- `auth-webauthn`
  - `PasskeyAuthenticationProviderTest`
  - `PasskeyRegistrationServiceTest`

## GitHub Actions

### 현재 워크플로우 파일

- `.github/workflows/build.yml`
- `.github/workflows/publish.yml`

### `_gradle.yml`

- 재사용 워크플로우입니다.
- 공통 수행:
  1. `actions/checkout`
  2. `actions/setup-java`
  3. `gradle/actions/setup-gradle`
  4. `./gradlew <task> --no-daemon --stacktrace`
- 필요 시 `release_version`을 Gradle project property로 주입합니다.

### `build.yml`

- 트리거:
  - `main` 대상 PR
  - `main` push
- 수행:
  - `./gradlew clean test --no-daemon --stacktrace`

### `publish.yml`

- 트리거: `v*` 태그 push
- 수행:
  1. 태그에서 `release_version` 계산
  2. `_gradle.yml`을 호출해 `test` 실행
  3. `_gradle.yml`을 호출해 `publishAggregationToCentralPortal` 실행
  4. Central Portal에 배포

## 참고

- CI와 문서는 소스 트리 기준으로 설명합니다.
- generated build 산출물은 문서 기준이 아닙니다.
