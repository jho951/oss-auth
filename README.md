# auth

[![Build](https://github.com/jho951/auth/actions/workflows/build.yml/badge.svg)](https://github.com/jho951/auth/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.jho951/auth-core?label=maven%20central)](https://central.sonatype.com/search?q=jho951)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue)](./LICENSE)
[![Tag](https://img.shields.io/github/v/tag/jho951/auth)](https://github.com/jho951/auth/tags)

## 설정

이 프로젝트의 공개 설정 및 정책 관련 계약은 [oss-contract](https://github.com/jho951/oss-contract)에서 관리됩니다.
설정 변경이 필요하거나 상세 스펙을 확인하려면 해당 저장소를 참고하세요.

## 공개 좌표

- `io.github.jho951:auth-bom`
- `io.github.jho951:auth-core`
- `io.github.jho951:auth-jwt`
- `io.github.jho951:auth-session`
- `io.github.jho951:auth-apikey`
- `io.github.jho951:auth-hmac`
- `io.github.jho951:auth-dpop`
- `io.github.jho951:auth-mfa`
- `io.github.jho951:auth-mtls`
- `io.github.jho951:auth-oidc`
- `io.github.jho951:auth-otp`
- `io.github.jho951:auth-saml`
- `io.github.jho951:auth-service-account`
- `io.github.jho951:auth-webauthn`

## 제공 모듈

- `auth-bom`: 공식 지원 모듈 버전을 묶는 BOM
- `auth-core`: 공통 모델과 연동 포인트
- `auth-jwt`: JWT 발급/검증, key resolver, claim mapper
- `auth-session`: 세션 저장소, 세션 record, 만료 전략
- `auth-apikey`: API key 인증 capability
- `auth-hmac`: HMAC 서명 인증 capability
- `auth-dpop`: DPoP proof 검증과 sender-constrained token helper
- `auth-mfa`: MFA step-up 정책, factor verifier, principal 승격 helper
- `auth-mtls`: mTLS client certificate 인증과 certificate-bound token helper
- `auth-oidc`: OIDC ID token 검증과 principal mapping capability
- `auth-otp`: HOTP, TOTP, recovery code 검증 helper
- `auth-saml`: SAML assertion 검증과 principal mapping capability
- `auth-service-account`: service account 인증 capability
- `auth-webauthn`: WebAuthn/passkey assertion, attestation, principal mapping capability

## 빠른 시작

```gradle
repositories {
    mavenCentral()
}

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

## 문서

- [docs/README.md](docs/README.md)
- [구현 가이드](docs/implementation-guide.md)
- [소비 규약](docs/consumption-conventions.md)
- [모듈 lifecycle](docs/module-lifecycle.md)
- [기여 가이드](CONTRIBUTING.md)
