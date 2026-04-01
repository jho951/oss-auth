# 릴리즈 가이드

## 현재 퍼블리싱 구조

루트 `build.gradle`은 각 서브프로젝트를 Central Portal 경유 Maven Central publish 경로로 연결합니다.
현재 artifactId는 `project.name`을 그대로 사용합니다.

예:

- `io.github.jho951:auth-core:<version>`
- `io.github.jho951:auth-common-test:<version>`
- `io.github.jho951:auth-jwt:<version>`
- `io.github.jho951:auth-session:<version>`
- `io.github.jho951:auth-hybrid:<version>`
- `io.github.jho951:auth-spring:<version>`
- `io.github.jho951:auth-spring-boot-starter:<version>`

## 트리거

- Git tag `v*` push
- 또는 `workflow_dispatch`
- 현재 릴리스 예시: `v1.0`

## 환경 변수 / 시크릿

- `MAVEN_CENTRAL_USERNAME` - Central Portal user token username
- `MAVEN_CENTRAL_PASSWORD` - Central Portal user token password
- `MAVEN_CENTRAL_GPG_PRIVATE_KEY`
- `MAVEN_CENTRAL_GPG_PASSPHRASE`
- `MAVEN_CENTRAL_NAMESPACE` (선택, 자동 publish 단계에서 사용)

## 릴리즈 절차 예시

```bash
git add -A
git commit -m "release: v1.0"
git tag -a v1.0 -m "release: v1.0"
git push origin main
git push origin v1.0
```

## 주의사항

- 문서에서 `auth-core`, `auth-spring-boot-starter` 같은 좌표를 언급할 때는 현재 퍼블리싱 좌표와 혼동하면 안 됩니다.
- 현재 publish는 `artifactId = project.name` 규칙을 따릅니다.
