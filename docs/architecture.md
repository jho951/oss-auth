# 아키텍처

## 책임

### 공통 및 인프라 계층
- `auth-bom`
  - 현재 공식 지원하는 auth 모듈 버전을 하나의 BOM으로 제공합니다.
  - 2계층과 소비 애플리케이션이 모듈 버전을 개별로 맞추지 않도록 조율합니다.
- `auth-core`
  - 인증 시스템의 추상화 및 공통 인터페이스를 정의합니다.
  - 모든 인증 수단이 공통으로 사용하는 도메인 모델(User, Principal), 예외 처리, 그리고 하위 모듈이 구현해야 할 연동 포인트(SPI)를 제공합니다.

### 사용자 기반 인증
- `auth-jwt`
  - Stateless한 인증을 책임집니다.
  - JSON Web Token의 생성, 서명 검증, 만료 관리 및 클레임 파싱을 전담하며, 서버 간 확장성이 필요한 환경에 대응합니다.
- `auth-session`
  - 서버 메모리나 저장소(Redis 등)를 활용한 상태 유지(Stateful) 인증을 구현합니다.
  - 전통적인 브라우저 환경에서의 세션 생성, 유지 및 폐기 로직을 관리합니다.
- `auth-oidc`
  - 외부 ID 제공자(Google, Kakao 등)를 통한 신원 보증 표준(OpenID Connect)을 처리합니다.
  - 인가 코드 교환, ID 토큰 검증 등 소셜 로그인 연동의 복잡함을 캡슐화합니다.
- `auth-mfa`
  - 추가 인증(step-up)이 필요한지 판단하는 정책과 2차 factor 검증 조립을 처리합니다.
  - 위험도나 보호 액션에 따라 TOTP, passkey, recovery code 같은 factor를 검증하고 `Principal`을 승격하는 경계를 제공합니다.
- `auth-webauthn`
  - passkey/WebAuthn assertion, attestation 검증 계약과 principal mapping 경계를 제공합니다.
  - browser ceremony 자체는 포함하지 않고 1계층 검증 포인트만 제공합니다.

### 시스템 및 API 보안
- `auth-apikey`
  - 가장 단순한 형태의 클라이언트 식별을 담당합니다.
  - 발급된 고유 키를 기반으로 호출자를 식별하고, 요청 헤더를 통한 간단한 접근 제어를 수행합니다.
- `auth-hmac`
  - 메시지 무결성 및 보안 강화 인증을 책임집니다.
  - 페이로드와 비밀키를 조합한 해시(Hash) 값을 검증하여, 요청 데이터가 전송 중 변조되지 않았음을 보장합니다.
- `auth-otp`
  - TOTP/HOTP와 recovery code 같은 일회용 인증수단 검증 helper를 제공합니다.
- `auth-dpop`
  - sender-constrained token을 위한 DPoP proof 검증과 access token hash binding을 담당합니다.
- `auth-mtls`
  - client certificate 인증과 certificate-bound token 확인을 담당합니다.
- `auth-saml`
  - 외부 SAML assertion 검증과 principal mapping capability를 제공합니다.
- `auth-service-account`
  - 비인간 주체(서버, 배치 봇 등)의 인증을 전담합니다.
  - 사용자 로그인 없이도 시스템 간 통신이 가능하도록 전용 계정과 권한(RBAC)을 부여하고 관리합니다.
  - X.509 workload identity와 SPIFFE류 principal mapping도 이 경계에서 처리할 수 있습니다.

## 원칙

### 관심사의 분리
- 신원 확인 집중: 인증(Authentication)은 주체(Principal)의 신원을 확인하는 것에만 집중합니다.
- 권한 부여 배제: 권한 판단(Authorization)은 애플리케이션 또는 상위 정책 계층의 책임으로 남겨둡니다.

### 비즈니스 중립성
- 정책 결정권 위임: 기본 구현을 제공하되, 특정 서비스의 비즈니스 규칙이나 최종 권한 정책을 라이브러리 내부에서 결정하지 않습니다.
- 계정 운영 정책 분리: 비밀번호 만료, 계정 잠금 등 사용자 계정 관리(User Management) 기능은 포함하지 않습니다.
- 인증원 조합 외부화: JWT, session, API key 같은 여러 인증 수단의 조합 순서와 fallback 정책은 상위 계층에서 결정합니다.

### 무상태성 및 독립성
- 환경 하드코딩 금지: 특정 URL 패턴, 조직 특유의 헤더, 서비스 경계(Boundary) 규칙을 코드 내부에 포함하지 않습니다.
- 프레임워크 비의존: Spring, Servlet, WebFlux 등 특정 웹 프레임워크에 종속적인 통합(Integration) 로직을 포함하지 않고 순수 자바 기반의 핵심 로직을 유지합니다.

### 수평적 확장 전략
- 인증 수단의 확장: 새로운 기능 확장은 인증 메커니즘(Method) 자체를 다양화하는 방향으로 진행하며, 인증 단계 사이에 도메인 로직을 추가하여 수직적으로 복잡도를 높이지 않습니다.
