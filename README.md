# auth

[![Build](https://github.com/jho951/auth/actions/workflows/build.yml/badge.svg)](https://github.com/jho951/auth/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.jho951/auth-core?label=maven%20central)](https://central.sonatype.com/search?q=jho951)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue)](./LICENSE)
[![Tag](https://img.shields.io/github/v/tag/jho951/auth)](https://github.com/jho951/auth/tags)

## 공개 좌표

- `io.github.jho951:auth-core`
- `io.github.jho951:auth-jwt`
- `io.github.jho951:auth-session`
- `io.github.jho951:auth-hybrid`
- `io.github.jho951:auth-apikey`
- `io.github.jho951:auth-hmac`
- `io.github.jho951:auth-oidc`
- `io.github.jho951:auth-service-account`

## 제공 모듈

- `auth-core`: 공통 모델과 연동 포인트
- `auth-jwt`: JWT 발급/검증, key resolver, claim mapper
- `auth-session`: 세션 저장소, 세션 record, 만료 전략
- `auth-hybrid`: JWT와 세션 조합 메커니즘
- `auth-apikey`: API key 인증 capability
- `auth-hmac`: HMAC 서명 인증 capability
- `auth-oidc`: OIDC ID token 검증과 principal mapping capability
- `auth-service-account`: service account 인증 capability

## 빠른 시작

```gradle
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.jho951:auth-core:<version>")
    implementation("io.github.jho951:auth-jwt:<version>")
    implementation("io.github.jho951:auth-session:<version>")
    implementation("io.github.jho951:auth-hybrid:<version>")
    implementation("io.github.jho951:auth-apikey:<version>")
    implementation("io.github.jho951:auth-hmac:<version>")
    implementation("io.github.jho951:auth-oidc:<version>")
    implementation("io.github.jho951:auth-service-account:<version>")
}
```

## 문서

- [docs/README.md](docs/README.md)
- [구현 가이드](docs/implementation-guide.md)
- [기여 가이드](CONTRIBUTING.md)
