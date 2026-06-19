# 테스트/CI/배포 가이드

## 로컬 테스트 실행

### 전체 빌드

```bash
./gradlew clean build
```

### 전자정부 표준프레임워크 호환성 검증

```bash
./gradlew egovframeCompatibilityCheck
```

- Java 8 bytecode 기준을 확인합니다.
- Spring, Spring Boot, Servlet, Jakarta, eGovFrame runtime 직접 의존성이 라이브러리 본체에 들어오지 않았는지 확인합니다.

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

## 로컬 배포 검증

### Maven Local publish

```bash
./gradlew publishToMavenLocal
```

### Central Portal publish

```bash
./gradlew publishAggregationToCentralPortal --no-daemon --stacktrace --no-configuration-cache
```

- 실제 배포에는 `MAVEN_CENTRAL_USERNAME`, `MAVEN_CENTRAL_PASSWORD`, `MAVEN_CENTRAL_GPG_PRIVATE_KEY`, `MAVEN_CENTRAL_GPG_PASSPHRASE`가 필요합니다.
- `publishAggregationToCentralPortal`는 현재 NMCP aggregation 태스크 특성상 configuration cache 없이 실행합니다.

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
  4. `release_version`이 있으면 `ORG_GRADLE_PROJECT_release_version`으로 주입
  5. `./gradlew <task> --no-daemon --stacktrace` 실행
- `publishAggregationToCentralPortal`를 실행할 때만 `--no-configuration-cache`를 추가합니다.

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
- 태그 `v4.0.0`은 workflow 안에서 `release_version=4.0.0`으로 변환됩니다.
- 이 값은 workflow 실행 중 `gradle.properties`의 `release_version`보다 우선합니다.
- 필요한 GitHub Actions secret:
  - `MAVEN_CENTRAL_USERNAME`
  - `MAVEN_CENTRAL_PASSWORD`
  - `MAVEN_CENTRAL_GPG_PRIVATE_KEY`
  - `MAVEN_CENTRAL_GPG_PASSPHRASE`

## 참고

- CI와 문서는 소스 트리 기준으로 설명합니다.
- generated build 산출물은 문서 기준이 아닙니다.
