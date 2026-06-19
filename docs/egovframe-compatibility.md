# 전자정부 표준프레임워크 호환성

이 저장소는 전자정부 표준프레임워크 실행환경 안에서 사용할 수 있는 순수 Java 인증 라이브러리로 유지합니다.

## 기준

- Java 8 bytecode로 컴파일합니다.
- `org.springframework.*`, `org.springframework.boot.*`, `jakarta.*`, `javax.servlet.*`, `org.egovframe.rte.*`를 라이브러리 본체의 직접 의존성으로 두지 않습니다.
- Servlet filter, Spring MVC controller, Spring Security filter chain, Spring Boot auto-configuration은 이 저장소에 넣지 않습니다.
- eGovFrame 애플리케이션은 필요한 모듈만 Maven/Gradle 의존성으로 추가하고, request adapter와 보안 정책은 애플리케이션 계층에서 조립합니다.

공식 실행환경 릴리즈 노트 기준으로 eGovFrame 4.0.0은 JDK 1.8 class compile로 전환했고, 4.3.0은 Spring Boot 2.7.18/Spring Framework 5.3.37 기반입니다. 5.0.0은 Spring Boot 3.5.6/Spring Framework 6.2.11 기반입니다.

공식 릴리즈 노트: https://www.egovframe.go.kr/home/sub.do?menuNo=33

## Maven 예시

eGovFrame 템플릿 프로젝트가 Maven을 쓰는 경우 BOM을 `dependencyManagement`에 import하고 필요한 모듈만 추가합니다.

```xml
<properties>
    <oss-auth.version>4.0.0</oss-auth.version>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.jho951</groupId>
            <artifactId>auth-bom</artifactId>
            <version>${oss-auth.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>io.github.jho951</groupId>
        <artifactId>auth-core</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.jho951</groupId>
        <artifactId>auth-jwt</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.jho951</groupId>
        <artifactId>auth-session</artifactId>
    </dependency>
</dependencies>
```

## Gradle 예시

```gradle
dependencies {
    implementation(platform("io.github.jho951:auth-bom:4.0.0"))

    implementation("io.github.jho951:auth-core")
    implementation("io.github.jho951:auth-jwt")
    implementation("io.github.jho951:auth-session")
}
```

## Adapter 작성 기준

eGovFrame 4.x는 Spring 5/javax 계열이고, eGovFrame 5.x는 Spring 6/jakarta 계열입니다. 둘을 동시에 지원하려면 이 저장소 본체에 framework type을 넣지 말고 별도 adapter 모듈이나 소비 애플리케이션에서 다음 형태로 연결합니다.

```text
HttpServletRequest -> credential 추출 -> auth-* provider 호출 -> Principal -> 애플리케이션 보안 컨텍스트 반영
```

이 저장소가 제공하는 것은 `credential -> verify -> Principal` 경계까지입니다.

## 로컬 검증

```bash
./gradlew egovframeCompatibilityCheck
```

이 태스크는 각 라이브러리 모듈이 Java 8 bytecode를 유지하는지와 eGovFrame/Spring/Jakarta/Servlet 직접 의존성이 들어오지 않았는지 확인합니다.
