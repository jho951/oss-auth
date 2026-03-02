# 릴리즈 가이드

## 버전 관리

- 루트 버전: `build.gradle`의 `allprojects.version`
- 현재 값: `1.1.0`
- Java/Spring/JJWT/JUnit 버전: `gradle.properties`

## 배포 좌표 규칙

각 모듈은 `auth-{module}` artifactId로 배포됩니다.

예:
- `auth-contract`
- `auth-core`
- `auth-spi`
- `auth-starter`
- `auth-common`

관련 설정:
- `build.gradle`의 `publishing.publications.mavenJava`

## 릴리즈 절차

1. 버전 업데이트
2. 로컬 검증

```bash
./gradlew clean build
```

3. 태그 생성/푸시

```bash
git add -A
git commit -m "release: vX.Y.Z"
git tag -a vX.Y.Z -m "release: vX.Y.Z"
git push origin main
git push origin vX.Y.Z
```

4. GitHub Actions `publish.yml`에서 패키지 배포 확인

## 실패 시 점검

- `MAVEN_CENTRAL_USERNAME`, `MAVEN_CENTRAL_PASSWORD` 유효성
- `MAVEN_CENTRAL_GPG_PRIVATE_KEY`, `MAVEN_CENTRAL_GPG_PASSPHRASE` 유효성
- 태그 형식이 `v*` 패턴인지
- Sonatype(OSSRH) 계정/권한 확인
