# Docs

## Guide

### 시작할 때

1. [아키텍처](./architecture.md)
2. [모듈 가이드](./modules.md)
3. [전자정부 표준프레임워크 호환성](./egovframe-compatibility.md)
4. [구현 가이드](./implementation-guide.md)
5. [소비 규약](./consumption-conventions.md)
6. [SPI/extension 가이드](./extension-guide.md)

### 문제를 만났을 때

1. [트러블슈팅](./troubleshooting.md)

### 품질과 배포

1. [테스트/CI/배포 가이드](./test-and-ci.md)

## 읽는 순서

- 처음 사용하는 사람은 `아키텍처`, `모듈 가이드`, `전자정부 표준프레임워크 호환성`, `구현 가이드`, `SPI/extension 가이드` 순서로 보면 됩니다.
- 구체적인 attribute key와 저장 포맷 약속은 `소비 규약` 문서를 기준으로 맞춥니다.
- 현재 지원 모듈과 제거된 모듈은 `모듈 가이드` 문서를 기준으로 봅니다.
- 테스트를 돌리거나 배포 흐름을 확인할 때는 `테스트/CI/배포 가이드`를 봅니다.
- SPI를 직접 구현하는 경우 `SPI/extension 가이드`를 먼저 보세요.
- Spring, Servlet, WebFlux 같은 adapter는 이 저장소의 순수 1계층 본체에 포함하지 않습니다.
