# 보안 보강 로드맵

이 문서는 `auth`를 보안적으로 적용 가능한 오픈소스 인증 코어로 발전시키기 위한 우선순위를 정리합니다.

현재 저장소의 기본 방향은 유지합니다.

- 본체는 순수 Java 인증 라이브러리로 유지합니다.
- Spring, Servlet, WebFlux, eGovFrame adapter는 본체에 직접 넣지 않습니다.
- URL 정책, 조직별 header 규칙, 도메인 권한 판단, gateway routing은 상위 계층 책임으로 둡니다.
- 보안 보강은 credential 검증, token/session 안전성, 저장소 구현, 샘플, 검증 가능한 문서와 CI를 중심으로 진행합니다.

## 우선순위

### 1. 보안 기준 매핑 문서

목표:

- 현재 모듈이 어떤 보안 요구를 만족하는지 사용자가 빠르게 판단할 수 있게 합니다.
- OWASP ASVS, NIST Digital Identity Guidelines 같은 외부 기준과 저장소의 모듈 책임을 연결합니다.

산출물:

- `docs/security-baseline.md`
- 모듈별 보안 기준 매핑표
- 지원하는 위협 완화 범위와 지원하지 않는 범위
- 상위 애플리케이션이 반드시 구현해야 하는 책임 목록

포함할 내용:

- `auth-webauthn`: phishing-resistant MFA/passkey 검증 경계
- `auth-mfa`: step-up 정책과 factor 검증 경계
- `auth-jwt`: token signing, verification, key rotation, claim mapping 경계
- `auth-dpop`, `auth-mtls`: sender-constrained token과 token theft 완화 경계
- `auth-session`: session id, expiration, revocation, concurrent session 경계
- `auth-core`: 인증 결과 모델, SPI, refresh token 저장 계약

완료 기준:

- 각 공식 모듈마다 보안상 제공하는 것과 제공하지 않는 것이 구분되어 있어야 합니다.
- 사용자가 이 라이브러리만으로 해결할 수 없는 영역을 명확히 알 수 있어야 합니다.
- 보안 기준 문서가 구현 가이드와 extension 가이드에서 참조되어야 합니다.

### 2. 운영 저장소 구현

목표:

- 샘플 수준이 아니라 실제 서비스에서 바로 붙일 수 있는 refresh token, session, revocation 저장소 구현을 제공합니다.
- 여러 인스턴스 환경에서 token/session 폐기와 만료 정책을 일관되게 적용할 수 있게 합니다.

후보 모듈:

- `auth-store-jdbc`
- `auth-store-redis`
- `auth-token-revocation`

산출물:

- `RefreshTokenStore`의 JDBC 또는 Redis 구현
- `SessionStore`, `SessionRecordStore`의 JDBC 또는 Redis 구현
- `TokenRevocationStore` 구현
- refresh token hash 저장 helper
- refresh token rotation과 reuse detection 예제

완료 기준:

- refresh token 원문을 저장하지 않는 구현 경로가 제공되어야 합니다.
- token 재사용 탐지 시 폐기 또는 실패 처리 정책을 테스트로 검증해야 합니다.
- session expiration, revocation, concurrent session 흐름이 테스트로 검증되어야 합니다.
- 저장소 모듈은 본체와 동일하게 특정 웹 프레임워크에 의존하지 않아야 합니다.

### 3. Passkey/WebAuthn 적용성 강화

목표:

- `auth-webauthn`을 보안 관점의 대표 기능으로 강화합니다.
- 비밀번호 의존도를 줄이거나 MFA를 phishing-resistant 방식으로 구성하려는 사용자가 바로 따라할 수 있게 합니다.

산출물:

- passkey registration 흐름 예제
- passkey authentication 흐름 예제
- challenge 저장소 계약 또는 예제 구현
- rpId, origin, challenge, sign counter 검증 가이드
- credential 저장 포맷 예시

완료 기준:

- 등록과 로그인 흐름이 end-to-end 문서로 설명되어야 합니다.
- replay 방지와 challenge 만료 처리가 문서와 테스트에 반영되어야 합니다.
- browser ceremony는 상위 계층 책임으로 유지하되, 본체가 검증해야 하는 값은 명확히 구분해야 합니다.

### 4. JWT 보안 강화

목표:

- JWT 사용자가 실수하기 쉬운 issuer, audience, key rotation, token type, revocation 경계를 명확히 합니다.
- 기본 구현과 확장 포인트가 fail-closed 방식으로 동작하도록 테스트를 보강합니다.

산출물:

- issuer/audience validator helper 또는 문서화된 claim mapper 예제
- remote JWKS cache resolver 검토
- unknown `kid` 실패 테스트
- access token과 refresh token type 혼용 방지 테스트
- 짧은 access token 만료와 refresh token rotation 조합 가이드

완료 기준:

- 알 수 없는 `kid`가 기본 key로 조용히 fallback하지 않아야 합니다.
- token type이 맞지 않으면 인증이 실패해야 합니다.
- issuer/audience 검증을 어디에서 강제할지 문서로 명확히 해야 합니다.
- refresh token은 저장소 기준 상태 확인과 함께 사용하는 흐름이 문서화되어야 합니다.

### 5. 공급망 보안과 릴리즈 신뢰성

목표:

- 보안 라이브러리로서 릴리즈 산출물과 의존성 상태를 검증 가능한 형태로 제공합니다.
- 사용자가 Maven Central artifact를 신뢰하고 도입할 수 있게 합니다.

산출물:

- CodeQL 또는 정적 분석 workflow
- Dependabot 또는 Renovate 설정
- dependency vulnerability scan
- SBOM 생성
- release provenance 또는 artifact signing 문서

완료 기준:

- pull request에서 테스트와 정적 분석이 실행되어야 합니다.
- 릴리즈 시점에 배포 artifact, source jar, javadoc jar, signature 상태를 확인할 수 있어야 합니다.
- dependency 업데이트 정책과 취약점 대응 기준이 문서화되어야 합니다.

## 진행 원칙

- 새 기능은 먼저 보안 기준 문서에 책임 경계를 적고 구현합니다.
- 본체에 framework runtime 의존성을 추가하지 않습니다.
- 운영 구현은 별도 모듈로 추가하고 BOM에 포함 여부를 명확히 기록합니다.
- 샘플 앱은 본체와 분리된 예제로 관리합니다.
- 각 단계는 문서, 테스트, CI 중 최소 하나 이상의 검증 지점을 가져야 합니다.

## 권장 진행 순서

1. `docs/security-baseline.md`를 추가해 현재 모듈의 보안 기준과 책임 경계를 먼저 고정합니다.
2. `auth-store-jdbc` 또는 `auth-store-redis` 중 하나를 먼저 구현해 refresh token/session/revocation 운영 경로를 만듭니다.
3. `auth-webauthn` 중심의 passkey 적용 예제를 보강합니다.
4. `auth-jwt`의 issuer/audience/key rotation/fail-closed 테스트를 보강합니다.
5. GitHub Actions에 정적 분석, 의존성 점검, SBOM 생성 흐름을 추가합니다.
