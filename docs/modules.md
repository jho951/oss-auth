# 모듈 가이드

이 문서는 현재 저장소 기준으로 공식 지원 모듈, 제거된 모듈, 모듈별 책임 경계를 함께 설명합니다.

## 공식 지원 모듈

| 모듈 | 상태 | 역할 |
|------|------|------|
| `auth-bom` | active | 공식 지원 모듈 버전을 한 번에 가져오는 BOM |
| `auth-core` | active | 공통 모델과 인증 연동 포인트 |
| `auth-jwt` | active | JWT를 만들고 검증하는 구현 |
| `auth-session` | active | 세션 저장소와 세션 인증 로직을 다루는 구현 |
| `auth-apikey` | active | API key 인증 capability |
| `auth-hmac` | active | HMAC 서명 인증 capability |
| `auth-dpop` | active | DPoP proof 검증 capability |
| `auth-mfa` | active | MFA step-up 정책과 factor 검증 capability |
| `auth-mtls` | active | mTLS client certificate와 certificate-bound token capability |
| `auth-oidc` | active | OIDC ID token 검증과 principal mapping capability |
| `auth-otp` | active | HOTP, TOTP, recovery code verification capability |
| `auth-saml` | active | SAML assertion 검증과 principal mapping capability |
| `auth-service-account` | active | service account 인증 capability |
| `auth-webauthn` | active | WebAuthn assertion/attestation과 passkey principal mapping capability |

## 읽는 법

- 공통 모델과 연동 포인트를 먼저 이해하려면 `auth-core`부터 보면 됩니다.
- 2계층이나 최종 애플리케이션은 `auth-bom`을 먼저 import한 뒤 필요한 모듈만 선택하면 됩니다.
- 세션 저장과 principal 매핑이 필요하면 `auth-session`을 같이 보면 됩니다.
- 새 인증 수단을 추가하려면 capability 모듈이 `auth-core`의 `AuthenticationProvider`와 `Principal` 계약을 따르게 하면 됩니다.
- 실제 조립 예시는 [구현 가이드](./implementation-guide.md)를 보면 됩니다.
- 조립 시 사용할 attribute key와 저장 포맷 약속은 [소비 규약](./consumption-conventions.md)을 기준으로 맞춥니다.

## 책임 경계

- `auth-core`
  - 인증의 핵심 모델과 SPI를 제공합니다.
  - `Principal`, `User`, `Tokens`, `AuthException`, `AuthFailureReason`은 이 경계 안에서만 의미가 바뀌지 않아야 합니다.
  - `UserFinder`, `PasswordVerifier`, `UserStatusChecker`, `TokenService`, `RefreshTokenStore`, `OAuth2PrincipalResolver`는 외부 구현이 따라야 하는 계약입니다.
  - `AuthenticationProvider`, `AuthenticationResult`, `AuthenticationSource`, `AuthoritySet`, `AuthenticatedSubject`는 인증 결과를 서비스 정책 없이 표현합니다.
- `auth-bom`
  - 현재 공식 지원하는 auth 모듈 집합과 버전을 고정합니다.
  - 2계층과 소비 애플리케이션은 이 BOM을 통해 버전 정합성을 맞춥니다.
- `auth-jwt`
  - JWT 발급/검증 규칙을 구현합니다.
  - 서명 방식, `kid` 기반 key 선택, 클레임 mapping은 SPI로 교체할 수 있습니다.
- `auth-session`
  - 세션 식별자 발급, 저장, 조회, principal 매핑을 담당합니다.
  - 세션 저장소, record metadata, expiration, concurrent session 전략은 구현 선택입니다.
- `auth-mfa`
  - MFA requirement 평가, factor enrollment 조회 계약, factor verifier, principal 승격 helper를 제공합니다.
  - risk 계산, challenge UI, OTP 전송, WebAuthn ceremony는 상위 계층 책임입니다.
- `auth-dpop`
  - DPoP proof JWT를 검증하고 `ath`, `htm`, `htu`, `jti` replay를 확인하는 helper를 제공합니다.
  - access token 발급 정책, nonce 정책, authorization server endpoint는 상위 계층 책임입니다.
- `auth-mtls`
  - client certificate 검증 조립과 `cnf.x5t#S256` 같은 certificate-bound token helper를 제공합니다.
  - TLS handshake, trust store, certificate chain 검증은 상위 계층 또는 인프라 책임입니다.
- `auth-otp`
  - HOTP/TOTP 생성·검증과 recovery code hash 검증 helper를 제공합니다.
  - OTP secret 발급, QR provisioning, code delivery, 사용 횟수 제한은 상위 계층 책임입니다.
- `auth-saml`
  - SAML assertion 검증 계약, conditions validator, principal mapping을 제공합니다.
  - metadata endpoint, browser redirect/post binding, XML parser 선택은 상위 계층 책임입니다.
- `auth-webauthn`
  - passkey credential record, assertion/attestation verifier 계약, principal mapping을 제공합니다.
  - browser ceremony, challenge endpoint, credential registration persistence는 상위 계층 책임입니다.
- `auth-apikey`, `auth-hmac`, `auth-oidc`, `auth-service-account`
  - 인증 수단 자체의 검증 계약과 provider만 제공합니다.
  - 어느 URL이나 서비스 boundary에 적용할지는 상위 계층 책임입니다.

## 1계층으로 유지하는 기준

- 포함 가능: 인증 수단, 토큰 처리 기술, 세션 처리 기술, principal/result 모델, SPI 구현체
- 포함 불가: 서비스 URL 정책, 조직 헤더 규약, 도메인 권한 모델, 프로젝트별 issuer/audience/default priority, 복수 인증원의 조합 순서와 폴백 정책
- framework integration은 generic이어도 1계층 본체가 아니라 adapter 계층으로 분리합니다.

## 배포 대상

- `auth-bom`
- `auth-core`
- `auth-jwt`
- `auth-session`
- `auth-apikey`
- `auth-hmac`
- `auth-dpop`
- `auth-mfa`
- `auth-mtls`
- `auth-oidc`
- `auth-otp`
- `auth-saml`
- `auth-service-account`
- `auth-webauthn`

## 제거된 모듈

| 모듈 | 상태 | 대체 경로 | 비고 |
|------|------|-----------|------|
| `auth-hybrid` | removed | 상위 계층에서 `auth-jwt`, `auth-session` 등 필요한 인증원을 직접 조합 | 현재 소스 트리와 `auth-bom`에서 제외됩니다. |

## 운영 기준

- 새 모듈이 공식 지원 대상이면 `auth-bom`에 추가합니다.
- 더 이상 유지하지 않는 모듈은 `auth-bom`에서 제외하고 이 문서에 기록합니다.
- 단순 rename인 경우에는 Maven Central에서 relocation POM을 별도로 발행합니다.
- 하나의 모듈이 여러 모듈로 분리된 경우에는 relocation 대신 migration 문서와 replacement 안내를 사용합니다.
