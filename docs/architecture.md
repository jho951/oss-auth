# 아키텍처

## 책임

- `auth-core`: 인증에서 공통으로 쓰는 모델과 연동 포인트
- `auth-jwt`: JWT 발급과 검증 구현
- `auth-session`: 세션 기반 인증 구현
- `auth-hybrid`: JWT와 세션을 함께 쓰는 조합 구현
- `auth-apikey`, `auth-hmac`, `auth-oidc`, `auth-service-account`: 새 인증 수단 capability

## 원칙

- 인증은 신원 확인에 집중합니다.
- 권한 판단은 애플리케이션이나 상위 정책 계층이 담당합니다.
- 기본 구현은 제공하되, 서비스의 비즈니스 규칙을 결정하지 않고, 권한 정책을 최종 판단하지 않습니다.
- 사용자 계정 운영 정책을 대신 관리하지 않습니다.
- 특정 URL, 특정 조직 헤더, 특정 service boundary 규칙을 하드코딩하지 않습니다.
- Spring, Servlet, WebFlux 같은 framework integration을 포함하지 않습니다.
- 1계층 확장은 인증 기능 자체를 늘리는 방향이어야 합니다.
