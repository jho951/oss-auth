# 소비 규약

원칙:

- 이 저장소는 1계층이므로 HTTP endpoint, 화면, redirect flow 규약은 정의하지 않습니다.
- 대신 capability 모듈 사이에서 재사용해야 하는 attribute key와 최소 저장 포맷만 고정합니다.
- 여기에 적힌 key 이름은 가능하면 그대로 유지하는 편이 모듈 조합과 문서 일관성에 유리합니다.

## 1. MFA 규약

`auth-mfa`의 기본 verifier는 아래 key를 기준으로 동작합니다.

### enrollment attribute

- `otp_secret`
  - `TotpMfaVerifier`가 읽는 Base32 공유 비밀값입니다.
- `recovery_hashes`
  - `RecoveryCodeMfaVerifier`가 읽는 복구 코드 해시 목록입니다.
  - 해시 값은 `Sha256RecoveryCodeVerifier#hash` 형식을 따르는 것이 기본입니다.

### proof attribute

- `code`
  - TOTP와 recovery code verifier가 모두 읽는 사용자 제출 코드입니다.

### verifier 검증 성공 후 추가 attribute

- `verified_by`
  - 어떤 verifier가 성공했는지 나타냅니다.
  - 기본값 예시: `totp`, `recovery_code`
- `totp_secret_attribute`
  - `TotpMfaVerifier`가 어떤 enrollment key를 사용했는지 기록합니다.
- `matched_recovery_code_hash`
  - 일치한 recovery code 해시 값을 기록합니다.

### principal 승격 후 추가 attribute

`DefaultMfaPrincipalMapper`는 검증 성공 후 반환되는 `Principal`에 아래 key를 추가합니다.

- `mfa_authenticated`
  - MFA step-up 성공 여부입니다. 기본값은 `true`입니다.
- `mfa_factor_id`
  - 성공한 enrollment의 factor id입니다.
- `mfa_factor_type`
  - 성공한 factor type을 소문자 문자열로 기록합니다. 예: `totp`, `recovery_code`
- `mfa_authenticated_at`
  - MFA step-up이 완료된 시각입니다.
- `mfa_action`
  - `MfaChallengeContext`에 action이 있으면 기록합니다.
- `mfa_risk_level`
  - `MfaChallengeContext`의 risk level 이름입니다.
- `amr`
  - 기존 `amr` 값에 성공한 factor type을 중복 없이 추가한 목록입니다.

## 2. WebAuthn 규약

`auth-webauthn`은 browser ceremony 전체를 소유하지 않고, 상위 계층이 수집한 값을 정규화해 검증합니다.

### 요청 필드

- `WebAuthnAuthenticationRequest.credentialId`
  - assertion에 사용된 credential id입니다.
- `WebAuthnAuthenticationRequest.clientDataJson`
  - 원본 `clientDataJSON` 문자열을 그대로 전달합니다.
- `WebAuthnAuthenticationRequest.authenticatorData`
  - Base64URL 인코딩된 원본 authenticator data를 전달합니다.
- `WebAuthnAuthenticationRequest.signature`
  - assertion signature 원문을 전달합니다.
  - 실제 형식 해석은 `WebAuthnAssertionSignatureValidator` 구현체 책임입니다.
- `WebAuthnAuthenticationRequest.userHandle`
  - 값이 있으면 저장된 credential의 `userHandle`과 비교합니다.
- `WebAuthnAuthenticationRequest.challenge`
  - 상위 계층이 발급하고 저장한 challenge 값입니다.
- `WebAuthnAuthenticationRequest.origin`
  - client data의 origin과 비교할 기대 origin입니다.
- `WebAuthnAuthenticationRequest.rpId`
  - authenticator data의 RP ID hash와 비교할 RP ID입니다.
- `WebAuthnRegistrationRequest.credentialId`
  - 새로 등록할 credential id입니다.
- `WebAuthnRegistrationRequest.clientDataJson`
  - 원본 `clientDataJSON` 문자열을 그대로 전달합니다.
- `WebAuthnRegistrationRequest.attestationObject`
  - attestation object 원문을 그대로 전달합니다.
  - 실제 파싱과 신뢰 체인 검증은 `WebAuthnAttestationStatementValidator` 구현체 책임입니다.
- `WebAuthnRegistrationRequest.challenge`
  - 상위 계층이 발급하고 저장한 registration challenge 값입니다.
- `WebAuthnRegistrationRequest.origin`
  - client data의 origin과 비교할 기대 origin입니다.
- `WebAuthnRegistrationRequest.rpId`
  - registration 흐름의 RP ID입니다.

`DefaultWebAuthnRequestValidator`는 인증 요청에서 `credentialId`, `clientDataJson`, `authenticatorData`, `signature`, `challenge`, `origin`, `rpId`가 비어 있지 않아야 한다고 봅니다. 등록 요청에서는 `credentialId`, `clientDataJson`, `attestationObject`, `challenge`, `origin`, `rpId`가 비어 있지 않아야 합니다.

### 요청 attribute

- `require_user_verification`
  - `true`이면 `DefaultWebAuthnAssertionVerifier`가 UV 플래그를 필수로 봅니다.

### credential 저장 모델

- `WebAuthnCredentialRecord.publicKeyCose`
  - assertion 서명 검증기가 이해할 수 있는 공개키 표현을 저장합니다.
  - 기본 필드명은 `publicKeyCose`지만, 실제 저장 형식은 상위 계층이 선택한 validator와 합의해야 합니다.
- `WebAuthnCredentialRecord.signCount`
  - 마지막으로 신뢰한 sign counter 값입니다.
- `WebAuthnCredentialRecord.userHandle`
  - 존재하면 assertion 요청의 `userHandle`과 일치 검사를 수행합니다.

### assertion 검증 성공 후 추가 attribute

- `user_present`
- `user_verified`
- `origin`
- `challenge`

## 3. JWT / DPoP / mTLS 규약

### JWT

- `JwtTokenService`는 기본적으로 `jti`를 발급합니다.
- `TokenRevocationStore`는 가능하면 `jti`를 key로 사용합니다.
- `DefaultJwtClaimsMapper`는 `token_type`, `authorities` claim을 기본 예약 값으로 사용합니다.

### DPoP

- proof JWT는 최소한 `jti`, `htm`, `htu`, `iat`를 포함해야 합니다.
- access token 바인딩을 사용할 때는 `ath`를 포함해야 합니다.
- `DpopProofVerifier`는 검증 성공 시 proof claim을 attribute로 유지하고, 추가로 `jwk` 공개키 정보를 넣습니다.

### mTLS

- certificate-bound token 확인 claim은 `cnf.x5t#S256` 형식을 사용합니다.
- `X509ThumbprintUtils`는 이 형식을 기준으로 비교합니다.

## 4. SPIFFE / workload identity 규약

`SpiffePrincipalResolver`는 아래 attribute를 기본으로 넣습니다.

- `credential_type`
  - 기본값: `x509_service`
- `trust_domain`
  - 예: `example.org`
- `workload_path`
  - 예: `/ns/payments/sa/billing`

상위 계층은 이 값을 그대로 쓰거나, 필요하면 추가 attribute를 더 얹으면 됩니다.

## 5. SAML 규약

- `DomSamlAssertionExtractor`는 `Attribute`의 `Name` 값을 `attributes` map key로 사용합니다.
- attribute 값이 하나면 `String`, 여러 개면 `List<String>`으로 담습니다.
- `SubjectConfirmationData`의 `Recipient`, `InResponseTo`를 기본 필드로 사용합니다.
- `Conditions`의 `NotBefore`, `NotOnOrAfter`를 시간 조건으로 사용합니다.

## 6. 상위 계층 책임

이 문서에 없는 항목은 기본적으로 상위 계층 책임으로 봅니다.

- challenge 발급과 저장
- OTP 발송
- WebAuthn ceremony 상태 관리
- SAML metadata endpoint와 binding 선택
- DPoP nonce 발급 정책
- trust store 운영과 certificate chain 수집
