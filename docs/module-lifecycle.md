# 모듈 lifecycle

이 문서는 현재 저장소 기준으로 어떤 모듈이 공식 지원 대상인지와 더 이상 유지하지 않는 모듈을 설명합니다.

## 현재 지원 모듈

| 모듈 | 상태 | 역할 |
|------|------|------|
| `auth-bom` | active | 공식 지원 모듈 버전을 묶는 BOM |
| `auth-core` | active | 공통 모델과 SPI |
| `auth-jwt` | active | JWT 발급과 검증 |
| `auth-session` | active | 세션 발급, 저장, 조회 |
| `auth-apikey` | active | API key 인증 capability |
| `auth-hmac` | active | HMAC 서명 인증 capability |
| `auth-dpop` | active | DPoP proof 검증 capability |
| `auth-mfa` | active | MFA step-up 정책과 factor 검증 capability |
| `auth-mtls` | active | mTLS client certificate와 certificate-bound token capability |
| `auth-oidc` | active | OIDC ID token 검증 capability |
| `auth-otp` | active | HOTP, TOTP, recovery code verification capability |
| `auth-saml` | active | SAML assertion 검증 capability |
| `auth-service-account` | active | service account 인증 capability |
| `auth-webauthn` | active | WebAuthn assertion/attestation capability |

## 더 이상 유지하지 않는 모듈

| 모듈 | 상태 | 대체 경로 | 비고 |
|------|------|-----------|------|
| `auth-hybrid` | removed | 상위 계층에서 `auth-jwt`, `auth-session` 등 필요한 인증원을 직접 조합 | 현재 소스 트리와 `auth-bom`에서 제외됩니다. |

## 운영 기준

- 새 모듈이 공식 지원 대상이면 `auth-bom`에 추가합니다.
- 더 이상 유지하지 않는 모듈은 `auth-bom`에서 제외하고 이 문서에 기록합니다.
- 단순 rename인 경우에는 Maven Central에서 relocation POM을 별도로 발행합니다.
- 하나의 모듈이 여러 모듈로 분리된 경우에는 relocation 대신 migration 문서와 replacement 안내를 사용합니다.
