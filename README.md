# auth

[![Build](https://github.com/jho951/oss-auth/actions/workflows/build.yml/badge.svg)](https://github.com/jho951/oss-auth/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.jho951/auth-core?label=maven%20central)](https://central.sonatype.com/search?q=jho951)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue)](./LICENSE)
[![Tag](https://img.shields.io/github/v/tag/jho951/oss-auth)](https://github.com/jho951/oss-auth/tags)

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

- [문서 인덱스](docs/README.md)
