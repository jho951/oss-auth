# 테스트/CI 가이드

## 로컬 테스트 실행

전체 빌드:

```bash
./gradlew clean build
```

모듈 단위 테스트:

```bash
./gradlew :common:test
./gradlew :contract:test
./gradlew :spi:test
./gradlew :core:test
./gradlew :support:test
./gradlew :boot-support:test
```

## 현재 테스트 범위

- `common`
  - `StringsTest`
- `contract`
  - `AuthExceptionTest`, `OAuth2UserIdentityTest`, `PrincipalTest`, `TokensTest`, `UserTest`
- `core`
  - `AuthServiceTest`
- `boot-support`
  - `RefreshCookieWriterTest`, `OAuth2AuthenticationSuccessHandlerTest`

## GitHub Actions

현재 워크플로우 파일:

- `.github/workflows/build.yml`
- `.github/workflows/publish.yml`
- `.github/workflows/discord-pr-notify.yml`

### `build.yml`

- 트리거: `main` 대상 PR, `main` push
- 수행: `./gradlew build --no-daemon --parallel --build-cache --stacktrace`
- 테스트 리포트 아티팩트 업로드 포함

### `publish.yml`

- 트리거: `v*` 태그 push, 수동 실행
- 수행:
  1. `./gradlew clean build`
  2. `./gradlew publish`
  3. 조건부로 Central Portal에 `publishing_type=automatic`으로 업로드 및 게시

### `discord-pr-notify.yml`

- PR opened / reopened 시 Discord webhook 알림

## 참고

업로드된 아카이브에 `build/`, `.gradle/`, `.idea/` 같은 산출물이 포함될 수 있지만, CI와 문서는 **소스 트리 기준**으로 설명합니다.
