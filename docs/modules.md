# 모듈 가이드

## 종류

- `auth-core`: 공통 모델과 인증 연동 포인트
- `auth-jwt`: JWT를 만들고 검증하는 구현
- `auth-session`: 세션 저장소와 세션 인증 로직을 다루는 구현
- `auth-hybrid`: JWT와 세션을 함께 쓰는 조합 로직
- `auth-apikey`: API key 인증 capability
- `auth-hmac`: HMAC 서명 인증 capability
- `auth-oidc`: OIDC ID token 검증과 principal mapping capability
- `auth-service-account`: service account 인증 capability

## 읽는 법

- 공통 모델과 연동 포인트를 먼저 이해하려면 `auth-core`부터 보면 됩니다.
- 세션 저장, principal 매핑, JWT와 세션 조합이 필요하면 `auth-session`과 `auth-hybrid`를 같이 보면 됩니다.
- 새 인증 수단을 추가하려면 capability 모듈이 `auth-core`의 `AuthenticationProvider`와 `Principal` 계약을 따르게 하면 됩니다.
- 실제 조립 예시는 [구현 가이드](./implementation-guide.md)를 보면 됩니다.

## 책임 경계

- `auth-core`
  - 인증의 핵심 모델과 SPI를 제공합니다.
  - `Principal`, `User`, `Tokens`, `AuthException`, `AuthFailureReason`은 이 경계 안에서만 의미가 바뀌지 않아야 합니다.
  - `UserFinder`, `PasswordVerifier`, `TokenService`, `RefreshTokenStore`, `OAuth2PrincipalResolver`는 외부 구현이 따라야 하는 계약입니다.
  - `AuthenticationProvider`, `AuthenticationResult`, `AuthenticationSource`, `AuthoritySet`, `AuthenticatedSubject`는 인증 결과를 서비스 정책 없이 표현합니다.
- `auth-jwt`
  - JWT 발급/검증 규칙을 구현합니다.
  - 서명 방식, `kid` 기반 key 선택, 클레임 mapping은 SPI로 교체할 수 있습니다.
- `auth-session`
  - 세션 식별자 발급, 저장, 조회, principal 매핑을 담당합니다.
  - 세션 저장소, record metadata, expiration, concurrent session 전략은 구현 선택입니다.
- `auth-hybrid`
  - JWT와 세션을 어떤 순서로 해석할지 결정하는 generic 조합 메커니즘을 제공합니다.
  - `HybridResolutionStrategy`와 `HybridConflictResolver`는 서비스 정책 없이 source priority와 충돌 처리를 표현합니다.
- `auth-apikey`, `auth-hmac`, `auth-oidc`, `auth-service-account`
  - 인증 수단 자체의 검증 계약과 provider만 제공합니다.
  - 어느 URL이나 서비스 boundary에 적용할지는 상위 계층 책임입니다.

## 1계층으로 유지하는 기준

- 포함 가능: 인증 수단, 토큰 처리 기술, 세션 처리 기술, principal/result 모델, SPI 구현체
- 포함 불가: 서비스 URL 정책, 조직 헤더 규약, 도메인 권한 모델, 프로젝트별 issuer/audience/default priority
- framework integration은 generic이어도 1계층 본체가 아니라 adapter 계층으로 분리합니다.

## 배포 대상

- `auth-core`
- `auth-jwt`
- `auth-session`
- `auth-hybrid`
- `auth-apikey`
- `auth-hmac`
- `auth-oidc`
- `auth-service-account`
