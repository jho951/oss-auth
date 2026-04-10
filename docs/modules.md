# 모듈 가이드

이 문서는 `auth` 저장소의 모듈이 각각 무엇을 하는지 한눈에 보이게 정리합니다.

## 모듈

- `auth-core`: 공통 모델, 인증 연동 포인트, `AuthService`
- `auth-jwt`: JWT를 만들고 검증하는 구현
- `auth-session`: 세션 저장소와 세션 인증 로직을 다루는 구현
- `auth-hybrid`: JWT와 세션을 함께 쓰는 조합 로직

## 읽는 법

- 공통 모델과 연동 포인트를 먼저 이해하려면 `auth-core`부터 보면 됩니다.
- 세션 저장, principal 매핑, JWT와 세션 조합이 필요하면 `auth-session`과 `auth-hybrid`를 같이 보면 됩니다.

## 책임 경계

- `auth-core`
  - 인증의 핵심 모델과 SPI를 제공합니다.
  - `Principal`, `User`, `Tokens`, `AuthException`, `AuthFailureReason`은 이 경계 안에서만 의미가 바뀌지 않아야 합니다.
  - `UserFinder`, `PasswordVerifier`, `TokenService`, `RefreshTokenStore`, `OAuth2PrincipalResolver`는 외부 구현이 따라야 하는 계약입니다.
- `auth-jwt`
  - JWT 발급/검증 규칙을 구현합니다.
  - 서명 방식과 토큰 클레임 정책은 구현 책임입니다.
- `auth-session`
  - 세션 식별자 발급, 저장, 조회, principal 매핑을 담당합니다.
  - 세션 저장소가 메모리인지 외부 저장소인지는 구현 선택입니다.
- `auth-hybrid`
  - JWT와 세션 중 어떤 순서로 해석할지 조합 규칙을 담당합니다.
  - 조합 우선순위는 구현 책임입니다.

## 배포 대상

- `auth-core`
- `auth-jwt`
- `auth-session`
- `auth-hybrid`
