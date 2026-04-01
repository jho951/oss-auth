# 릴리즈 가이드

## 현재 퍼블리싱 구조

루트 `build.gradle`은 각 서브프로젝트를 Central Portal 경유 Maven Central publish 경로로 연결합니다.
현재 artifactId는 `project.name`을 그대로 사용합니다.

예:

- `io.github.jho951:contract:<version>`
- `io.github.jho951:spi:<version>`
- `io.github.jho951:common:<version>`
- `io.github.jho951:core:<version>`
- `io.github.jho951:support:<version>`
- `io.github.jho951:boot-support:<version>`

## 트리거

- Git tag `v*` push
- 또는 `workflow_dispatch`

## 환경 변수 / 시크릿

- `MAVEN_CENTRAL_USERNAME` - Central Portal user token username
- `MAVEN_CENTRAL_PASSWORD` - Central Portal user token password
- `MAVEN_CENTRAL_GPG_PRIVATE_KEY`
- `MAVEN_CENTRAL_GPG_PASSPHRASE`
- `MAVEN_CENTRAL_NAMESPACE` (선택, 자동 publish 단계에서 사용)

## 릴리즈 절차 예시

```bash
git add -A
git commit -m "release: vX.Y.Z"
git tag -a vX.Y.Z -m "release: vX.Y.Z"
git push origin main
git push origin vX.Y.Z
```

## 주의사항

- 현재 아카이브에는 `grade.properties`라는 파일명이 보일 수 있습니다. Gradle의 일반적인 자동 로드 파일명은 `gradle.properties`입니다.
- 실제 배포 파이프라인에서 property 파일을 사용한다면 파일명 정합성을 먼저 확인하는 것이 좋습니다.
- 문서에서 목표 구조 artifact(`auth-core`, `auth-jwt-spring-boot-starter` 등)를 언급하더라도, 현재 퍼블리싱 좌표와 혼동하면 안 됩니다.
