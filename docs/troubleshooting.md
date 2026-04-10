# 트러블슈팅

## 1. `AuthService`를 생성할 수 없다

### 원인
- `AuthService`는 `UserFinder`, `PasswordVerifier`, `TokenService`, `RefreshTokenStore`가 있어야 생성됩니다.
- 호출하는 쪽이 필요한 SPI 구현을 주입하지 않았을 가능성이 큽니다.

### 조치
- 필요한 SPI 구현을 모두 준비합니다.

## 2. `AuthService.login(username, password)`에서 username이 비어 있다

### 원인
- username이 blank 입니다.

### 조치
- username 값을 채워서 호출합니다.

## 3. `AuthService.login(username, password)`에서 password가 비어 있다

### 원인
- password가 blank 입니다.

### 조치
- password 값을 채워서 호출합니다.

## 4. `AuthService.login(username, password)`에서 사용자를 찾지 못한다

### 원인
- `UserFinder`가 해당 username에 대한 사용자를 반환하지 않았습니다.

### 조치
- `UserFinder` 구현과 사용자 데이터 소스를 확인합니다.

## 5. `AuthService.login(username, password)`에서 비밀번호가 맞지 않는다

### 원인
- 저장된 비밀번호 해시와 입력 비밀번호가 일치하지 않습니다.
- `PasswordVerifier` 구현이 기대한 알고리즘과 다를 수 있습니다.

### 조치
- 비밀번호 해시 방식과 `PasswordVerifier` 구현을 맞춥니다.

## 6. `AuthService.login(Principal)`에 null이 들어간다

### 원인
- 외부에서 인증이 끝난 주체를 전달하지 않았습니다.

### 조치
- `Principal`을 먼저 생성한 뒤 호출합니다.

## 7. `AuthService.refresh(refreshToken)` 또는 `logout(refreshToken)`에서 refresh token이 비어 있다

### 원인
- refresh token이 blank 입니다.

### 조치
- refresh token 값을 채워서 호출합니다.

## 8. `AuthService.refresh(refreshToken)`에서 refresh token이 유효하지 않다

### 원인
- 서명 검증에 실패했거나 만료된 토큰입니다.
- `TokenService.verifyRefreshToken()`이 예외를 던졌습니다.

### 조치
- 토큰 형식, 서명키, 만료 시간, 토큰 타입을 확인합니다.

## 9. `AuthService.refresh(refreshToken)`에서 refresh token이 폐기되었다고 나온다

### 원인
- 서버 저장소에 해당 refresh token이 없습니다.
- 이전에 rotation 또는 logout으로 이미 폐기된 토큰입니다.

### 조치
- `RefreshTokenStore` 저장 상태를 확인하고 새로 로그인합니다.

## 10. `auth.jwt.secret must be at least 32 bytes for HS256`

원인:

- 현재 `JwtTokenService`는 HS256 기준 최소 32바이트 비밀키를 요구합니다.

조치:

- 더 긴 시크릿 사용
- 운영에서는 시크릿 매니저로 주입 권장

## 11. `JwtTokenService`가 토큰 검증에 실패한다

원인:

- 토큰 서명키가 다릅니다.
- 토큰이 만료되었거나 변조되었습니다.
- 토큰 타입이 access/refresh와 맞지 않습니다.

조치:

- 발급과 검증에 같은 서명키를 사용합니다.
- access token과 refresh token 타입을 확인합니다.
- 만료 시간과 시스템 시계를 확인합니다.

## 12. `Principal` 생성이 실패한다

### 원인
- `userId`가 blank 입니다.

### 조치
- `userId`를 채워서 생성합니다.

## 13. `User` 생성이 실패한다

### 원인
- `userId`, `username`, `passwordHash` 중 하나가 blank 입니다.

### 조치
- 사용자 최소 모델에 필요한 값을 모두 넣습니다.

## 14. `Tokens` 생성이 실패한다

### 원인
- access token 또는 refresh token이 blank 입니다.

### 조치
- 두 토큰 문자열을 모두 채워서 생성합니다.

## 15. `OAuth2UserIdentity` 생성이 실패한다

### 원인
- provider 또는 providerUserId가 blank 입니다.

### 조치
- 외부 provider 식별 정보를 모두 채워서 생성합니다.

## 16. 세션을 생성할 수 없다

### 원인
- `SessionService` 생성 시 필요한 저장소나 식별자 생성기가 주입되지 않았습니다.
- `create()`에 null principal이 들어갔습니다.

### 조치
- `SessionStore`, `SessionIdGenerator`, `Principal`을 모두 준비합니다.

## 17. 세션이 조회되지 않는다

### 원인
- session id가 blank 이거나 잘못됐습니다.
- `SessionStore`에 저장되지 않았습니다.
- 여러 인스턴스 환경에서 메모리 저장소를 쓰고 있습니다.

### 조치
- session id를 확인합니다.
- 저장소 구현과 만료 정책을 확인합니다.
- 운영에서는 외부 저장소를 사용합니다.

## 18. 세션 인증 결과가 예상과 다르다

### 원인
- `SessionPrincipalMapper`가 저장된 `Principal`을 원하는 형태로 매핑하지 않습니다.

### 조치
- 매핑 규칙을 확인하고 필요하면 구현을 교체합니다.

## 19. `HybridAuthenticationProvider`가 JWT 대신 세션을 사용한다

### 원인
- access token이 없거나 검증에 실패했습니다.
- 세션 식별자만 전달되어 세션 경로로 폴백했습니다.

### 조치
- JWT와 session 중 어떤 경로를 우선할지 확인합니다.
- 조합 순서를 바꾸려면 `HybridAuthenticationProvider` 구현을 교체합니다.

## 20. Gradle property가 안 읽힌다

원인:

- 현재 아카이브에 `gradle.properties`라는 파일명이 존재할 수 있음
- Gradle 일반 규약 파일명은 `gradle.properties`

조치:

- 파일명을 확인하고 필요하면 `gradle.properties`로 정리

## 21. artifact 이름이 문서와 다르다

원인:

- 현재 퍼블리싱은 `build.gradle`의 모듈별 `artifactId` 매핑을 사용

조치:

- 문서가 current implementation 문맥인지 먼저 확인
- 현재 좌표는 [modules.md](./modules.md) 참고
