# AGENTS.md (Instructions For Coding Agents)

This repository is a Java 21 / Spring Boot app built with Gradle. Architecture is a Spring Modulith modular monolith.

## Quick Facts
- Build tool: Gradle wrapper (`./gradlew`) using Gradle 8.x (`gradle/wrapper/gradle-wrapper.properties`).
- Runtime/toolchain: Java 21 (`build.gradle` toolchain). If Gradle says JAVA_HOME is missing, install JDK 21 and set `JAVA_HOME`.
- Modules (by package): `dev.prospectos.core`, `dev.prospectos.api`, `dev.prospectos.ai`, `dev.prospectos.infrastructure`.
- Tests: JUnit 5 + Spring Boot Test + Spring Modulith Test + AssertJ + Mockito.
- Default profile: `mock` (`src/main/resources/application.properties`).

## Build / Test / Run
### Common commands
```bash
./gradlew clean
./gradlew build
./gradlew test
./gradlew bootRun
```

### Run a single test (most important)
Single test class:
```bash
./gradlew test --tests 'dev.prospectos.core.domain.CompanyTest'
```
Single test method:
```bash
./gradlew test --tests 'dev.prospectos.core.domain.CompanyTest.someTestMethod'
```
Common subsets:
```bash
./gradlew test --tests '*IntegrationTest'
./gradlew test --tests '*ModulithTest'
```
Diagnostics:
```bash
./gradlew test --info
./gradlew test --stacktrace
```

### Coverage
- JaCoCo is configured; `test` finalizes `jacocoTestReport`.
- Report: `build/reports/jacoco/test/html/index.html`.

### Lint / formatting
- No enforced lint/formatter plugins (no Checkstyle/Spotless/PMD) in `build.gradle`.
- Follow `.editorconfig` (Java/Gradle/properties: 4 spaces; YAML/XML: 2 spaces; Java max line length: 120).

## Project Layout (Where Code Goes)
- Source: `src/main/java/dev/prospectos/**`
- Resources: `src/main/resources/**`
- Tests: `src/test/java/dev/prospectos/**` (integration tests in `src/test/java/dev/prospectos/integration`).
- Test config: `src/test/resources/application-test.properties`.
- Docs: `docs/` (update if behavior/module boundaries change).
- Generated output: `build/` (do not edit).

## Architecture & Modulith Boundaries (Critical)
- `core` = domain model + business rules. Must not depend on `ai` or `infrastructure`.
- `api` = cross-module service contracts + DTOs (keep stable; avoid Spring/JPA leakage here).
- `ai` = Spring AI client plumbing, provider selection, prompts/functions.
- `infrastructure` = web controllers, JPA adapters/repositories, scheduled jobs, integrations.
- Boundary assertions live in `src/test/java/dev/prospectos/modules/ModulithTest.java`.

## Configuration & Profiles
### Profiles
- `mock`: safe default; disables real AI provider autoconfig (`src/main/resources/application-mock.properties`).
- `development`: dev defaults; intended when no real API keys are configured (`src/main/resources/application-development.properties`).
- `test`: used by integration tests; uses H2 in-memory and disables providers by default (`src/test/resources/application-test.properties`).

### Dotenv
- `.env` is loaded by `dev.prospectos.config.DotenvEnvironmentPostProcessor` via `src/main/resources/META-INF/spring.factories`.
- Dotenv is intentionally skipped when `test` profile is active (keeps tests deterministic).
- `.env` is gitignored; reference is `.env.example`.

### AI provider keys (never commit secrets)
- Preferred env vars:
  - `SPRING_AI_OPENAI_API_KEY`
  - `SPRING_AI_ANTHROPIC_API_KEY`
  - `PROSPECTOS_AI_GROQ_API_KEY`
- Back-compat env vars also map through dotenv:
  - `OPENAI_API_KEY`, `ANTHROPIC_API_KEY`

## Coding Style (match existing code)
### Formatting
- 4-space indentation; keep diffs minimal; avoid large refactors/rewraps unrelated to the task.
- Prefer small, focused methods and clear naming over cleverness.

### Imports
- No wildcard imports.
- Keep imports grouped (typical order): `java.*`, `jakarta.*`, `org.*`, `dev.prospectos.*`, then `import static`.
- Remove unused imports.

### Naming
- Packages: `dev.prospectos.<module>.<subsystem>`.
- Types: PascalCase (`CompanyDataServiceJpa`).
- Methods/fields: camelCase (`createBestAvailableClient`).
- Constants: UPPER_SNAKE_CASE.
- Tests: `*Test` (unit) and `*IntegrationTest` (Spring context).

### Types & API design
- Use Java 21 features when they improve clarity (e.g., `record` for DTOs/result types; already used broadly).
- Prefer `Optional<T>` for absent lookups at repository/service boundaries.
- Use `@Nullable` only at module boundaries where null is expected; otherwise prefer `Optional`.

### Error handling & validation
- Domain (`core`) invariants: throw `IllegalArgumentException` for invalid inputs; `IllegalStateException` for invalid transitions.
- Web layer:
  - Use `ResponseStatusException(HttpStatus.NOT_FOUND, ...)` for missing resources.
  - Validation failures and `IllegalArgumentException` are mapped to HTTP 400 by `dev.prospectos.infrastructure.handler.ApiExceptionHandler`.
- Fail fast at boundaries (controller/service) with clear, user-safe messages.

### Logging
- Lombok `@Slf4j` is used; log intent and outcomes (not per-item spam).
- Never log secrets (API keys/tokens) or raw `.env` values.

### Lombok
- Lombok is enabled (compileOnly + annotationProcessor). Use sparingly; keep behavior explicit.

## Testing Guidelines
- Prefer fast unit tests with JUnit 5 + AssertJ.
- Use Mockito with `@ExtendWith(MockitoExtension.class)` for isolated services.
- Integration tests use `@SpringBootTest` and typically `@ActiveProfiles("test")`.
- Do not add network-dependent tests; keep AI paths mocked/disabled under the `test` profile.

## Other Agent/Tooling Rules
- Cursor rules: none found (`.cursor/rules/` and `.cursorrules` are absent).
- Copilot rules: none found (`.github/copilot-instructions.md` is absent).

## Security & Hygiene
- Never commit `.env` or credentials (see `.gitignore`; patterns include `*api-key*`, `*secret*`, `*token*`).
- Do not edit generated files in `build/`.
