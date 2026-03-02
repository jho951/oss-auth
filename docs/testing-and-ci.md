# 테스트/CI 가이드

## 로컬 테스트 실행

전체:

```bash
./gradlew clean build
```

특정 테스트:

```bash
./gradlew :starter:test --tests "com.auth.config.controller.RefreshCookieWriterTest"
```

## 현재 테스트 범위

- `common`
  - `StringsTest`
- `contract`
  - `AuthExceptionTest`
  - `ErrorCodeTest`
  - `PrincipalTest`
  - `TokensTest`
  - `UserTest`
- `starter`
  - `RefreshCookieWriterTest`

테스트 파일 위치:
- `common/src/test/java`
- `contract/src/test/java`
- `starter/src/test/java`

## GitHub Actions

워크플로우 파일:
- `.github/workflows/build.yml`
- `.github/workflows/publish.yml`
- `.github/workflows/discord-pr-notify.yml`

## `build.yml`

- 트리거: `main` 대상 PR, `main` push
- 수행: `./gradlew build --parallel --build-cache`
- 결과물: 테스트 리포트 아티팩트 업로드

## `publish.yml`

- 트리거: `v*` 태그 push, 수동 실행
- 수행:
  1. `./gradlew clean build`
  2. `./gradlew publish`
- 대상: Maven Central

## CI 환경 변수

- `MAVEN_CENTRAL_USERNAME`
- `MAVEN_CENTRAL_PASSWORD`
- `MAVEN_CENTRAL_GPG_PRIVATE_KEY`
- `MAVEN_CENTRAL_GPG_PASSPHRASE`

publish 시 credentials가 없으면 루트 `build.gradle`의 검증 로직에서 실패하도록 되어 있습니다.
