# Build, Test, Release

## 현재 저장소 기준 빌드

루트에서 다음 명령으로 빌드합니다.

```bash
./gradlew clean build
```

테스트까지 포함하려면 동일한 명령으로 충분합니다.

---

## 현재 루트 설정 특징

- `group = io.github.jho951`
- `version = findProperty("version")`
- Java toolchain 사용
- `maven-publish` + `signing` 공통 적용
- Maven Central publish 를 전제로 구성됨

---

## 현재 속성 파일

현재 저장소에는 `grade.properties` 파일이 존재합니다.

권장 변경:
- `grade.properties` → `gradle.properties`

현재 주요 속성:
- `java_version`
- `junit_version`
- `jjwt_version`
- `springBoot_version`
- `version`
- `encoding_method`

---

## 현재 배포 환경 변수

- `MAVEN_CENTRAL_USERNAME`
- `MAVEN_CENTRAL_PASSWORD`
- `MAVEN_CENTRAL_GPG_PRIVATE_KEY`
- `MAVEN_CENTRAL_GPG_PASSPHRASE`
- `MAVEN_CENTRAL_NAMESPACE` (자동 publish 시 필요)

---

## 목표 구조에서의 publish artifact

권장 artifact 예시:

- `io.github.jho951:auth-core:<version>`
- `io.github.jho951:auth-jwt:<version>`
- `io.github.jho951:auth-session:<version>`
- `io.github.jho951:auth-hybrid:<version>`
- `io.github.jho951:auth-spring:<version>`
- `io.github.jho951:auth-jwt-spring-boot-starter:<version>`
- `io.github.jho951:auth-session-spring-boot-starter:<version>`
- `io.github.jho951:auth-hybrid-spring-boot-starter:<version>`

---

## 권장 빌드 검증

### 전체 빌드
```bash
./gradlew clean build
```

### 특정 모듈만 빌드
```bash
./gradlew :auth-core:build
./gradlew :auth-jwt:build
./gradlew :auth-jwt-spring-boot-starter:build
```

### 샘플 실행
```bash
./gradlew :samples:sample-jwt-api:bootRun
```

---

## 릴리즈 체크리스트

1. version 확정
2. CHANGELOG / Release note 업데이트
3. 위키 문서 최신화
4. 샘플 애플리케이션 실행 검증
5. 태그 생성
6. Central Portal 자동 publish

---

## 권장 개선 사항

- `gradle.properties` 오타 수정
- 모듈 분리 후 root `build.gradle`의 공통 설정 정리
- starter 와 core 모듈의 javadoc / sources jar 유지
- 장기적으로 BOM 도입 여부 검토
