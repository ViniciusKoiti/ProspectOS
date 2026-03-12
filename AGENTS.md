# AGENTS.md (Instructions For Coding Agents)

This repository is evolving into a monorepo.

Current state:
- Backend: Java 21 / Spring Boot app built with Gradle
- Planned frontend: Flutter application in the same repository

Backend architecture remains a Spring Modulith modular monolith.

## Quick Facts
- Build tool: Gradle wrapper (`./gradlew`) using Gradle 8.x (`gradle/wrapper/gradle-wrapper.properties`).
- Runtime/toolchain: Java 21 (`build.gradle` toolchain). If Gradle says JAVA_HOME is missing, install JDK 21 and set `JAVA_HOME`.
- Modules (by package): `dev.prospectos.core`, `dev.prospectos.api`, `dev.prospectos.ai`, `dev.prospectos.infrastructure`.
- Tests: JUnit 5 + Spring Boot Test + Spring Modulith Test + AssertJ + Mockito.
- Default profile: `mock` (`src/main/resources/application.properties`).
- Monorepo direction: backend + Flutter app with clear workspace boundaries.

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
- Backend source: `src/main/java/dev/prospectos/**`
- Backend resources: `src/main/resources/**`
- Backend tests: `src/test/java/dev/prospectos/**` (integration tests in `src/test/java/dev/prospectos/integration`).
- Backend test config: `src/test/resources/application-test.properties`.
- Flutter app target location: `apps/flutter_app/`
- Week 2 web app (React) target location: `apps/prospectos-web/`
- Shared docs: `docs/` (update if behavior/module boundaries change).
- Generated output: `build/` (do not edit).

## Monorepo Direction
- Treat this repository as a monorepo from now on.
- Backend and Flutter code must stay in separate top-level work areas.
- Prefer this structure for new work:
  - `src/**` for the current Spring Boot backend
  - `apps/flutter_app/**` for the Flutter application
  - `apps/prospectos-web/**` for the Week 2 React web frontend track
  - `docs/**` for cross-cutting documentation
- Do not mix Flutter source into backend directories.
- If shared contracts are added later, document them explicitly before creating a shared package/module.

## MVP Week 2 Web Frontend Track (React)
- This repository currently includes a Week 2 web frontend delivery track using React + TypeScript.
- Keep all React code inside `apps/prospectos-web/**`.
- Treat this as a component-first implementation (reusable UI blocks before page-specific duplication).

### Week 2 day-by-day implementation docs
- `docs/milestones/semana-2/dia-1-fundacao-e-design-system.md`
- `docs/milestones/semana-2/dia-2-layout-e-dashboard.md`
- `docs/milestones/semana-2/dia-3-busca-e-formularios.md`
- `docs/milestones/semana-2/dia-4-resultados-e-icp-crud.md`
- `docs/milestones/semana-2/dia-5-companies-polimento-e-release.md`

### React component quality baseline
- UI primitives must exist under `components/ui` (`Button`, `Input`, `TextArea`, `Select`, `Modal`, `Table`, `Card`, loading/error states).
- Layout primitives must exist under `components/layout` (`Header`, `Sidebar`, `MainLayout`, `PageHeader`).
- Feature components must live under `components/features` and compose UI primitives.
- Avoid `any` in component props; prefer explicit typed interfaces.
- Standardize visual and behavioral states: `loading`, `error`, `empty`, `disabled`.
- Prefer `react-router-dom` links/navigation APIs over raw internal `a href`.

### React usage contract (must follow)
- React pages must orchestrate screen flow only; avoid embedding heavy business logic in page components.
- All backend communication must go through typed service modules (for example `services/api.ts` and feature service wrappers).
- Remote state must use React Query (`useQuery` for reads, `useMutation` for writes).
- Forms must use `react-hook-form` with schema validation (Zod) for payload safety.
- Every data-driven screen must implement explicit `loading`, `error`, and `empty` states.
- New UI work should compose existing primitives first; only add a new primitive when reuse is clear.
- Internal navigation must use `react-router-dom` (`Link`, `useNavigate`, route objects), not raw anchor refreshes.

### React anti-patterns (must avoid)
- Avoid `any` in API response types, component props, and form data.
- Avoid direct axios/fetch calls inside presentational `components/ui`.
- Avoid duplicating the same input/table/modal behavior in multiple features.
- Avoid coupling reusable UI components to endpoint-specific response shapes.
- Avoid shipping a screen that only handles success state and ignores error/loading behavior.

## Architecture & Modulith Boundaries (Critical)
- `core` = domain model + business rules. Must not depend on `ai` or `infrastructure`.
- `api` = cross-module service contracts + DTOs (keep stable; avoid Spring/JPA leakage here).
- `ai` = Spring AI client plumbing, provider selection, prompts/functions.
- `infrastructure` = web controllers, JPA adapters/repositories, scheduled jobs, integrations.
- Boundary assertions live in `src/test/java/dev/prospectos/modules/ModulithTest.java`.

## Flutter Architecture Direction
- The Flutter application must follow a component-oriented architecture.
- Prefer composition over large screens with embedded business logic.
- Organize Flutter code by feature first, then by component layers inside each feature.
- Recommended structure:
  - `apps/flutter_app/lib/app/` for app bootstrap, routing, theme, dependency setup
  - `apps/flutter_app/lib/features/<feature>/presentation/` for screens and UI flows
  - `apps/flutter_app/lib/features/<feature>/components/` for reusable feature components
  - `apps/flutter_app/lib/features/<feature>/domain/` for entities, use cases, contracts
  - `apps/flutter_app/lib/features/<feature>/data/` for DTOs, repositories, remote/local sources
  - `apps/flutter_app/lib/shared/components/` for design-system level reusable widgets
  - `apps/flutter_app/lib/shared/theme/` for tokens, typography, spacing, color system
- Keep widgets small and testable.
- Separate visual components from orchestration/state logic.
- Avoid dumping all reusable UI into one generic `widgets/` folder without feature boundaries.
- Prefer explicit design tokens and shared components over one-off styling.

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

### Lead search configuration
- `prospectos.leads.allowed-sources`: comma-separated list of allowed sources (default: `in-memory`)
- `prospectos.leads.default-icp-id`: default ICP ID to use when not provided in request (default: `1`)
  - If not set and request doesn't include `icpId`, lead search will return 400 error

## Coding Style (match existing code)
### Formatting
- 4-space indentation; keep diffs minimal; avoid large refactors/rewraps unrelated to the task.
- Prefer small, focused methods and clear naming over cleverness.

### Class size
- Prefer Java classes with up to 50 lines when practical.
- Acceptable hard limit is 100 lines per class.
- If a class exceeds 100 lines, split responsibilities or register an explicit temporary exception with a clear follow-up plan.

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
- Increasing test coverage is an active priority.
- When touching backend production code, prefer adding or updating tests in the same change when practical.
- Prefer targeted unit/integration tests that improve behavior confidence and JaCoCo coverage on changed code.
- When Flutter is added, require widget tests for reusable components and unit tests for presentation/domain logic where applicable.

## TDD Expectations
- Prefer TDD as the default development approach for new behavior and bug fixes.
- Follow the Red -> Green -> Refactor cycle whenever practical.
- Start by writing a failing test that describes the expected behavior or reproduces the bug.
- Implement the smallest production change needed to make the test pass.
- Refactor only after the behavior is protected by tests.
- Keep each TDD cycle small; avoid bundling many new behaviors into one test step.
- When a task is too broad for strict TDD, break it into smaller increments and apply TDD to each increment.
- For backend work, prefer unit tests first, then add integration tests only where wiring, persistence, configuration, or HTTP behavior matters.
- For Flutter work, prefer unit tests for logic, widget tests for components, and integration tests only for critical end-to-end flows.
- Before changing existing behavior, look for the closest existing test and extend it or add a neighboring test instead of creating scattered coverage.
- If a production change is made without a preceding failing test, document the reason in the final update.
- Bug fixes should usually begin with a regression test.
- Avoid deleting tests just to make the build pass unless the test is invalid and the reason is explicit.
- Treat tests as executable specification: names should describe behavior, not implementation details.
- Prefer deterministic tests over timing-sensitive or network-dependent tests.
- Keep mocks focused on boundaries; do not over-mock core domain behavior.

## Extreme Programming Expectations
- Use Extreme Programming practices pragmatically as the default engineering posture for this repository.
- Prefer small, frequent, low-risk changes over large rewrites.
- Keep commits focused, reviewable, and behaviorally coherent.
- Integrate continuously: avoid letting changes drift too far before validating them with tests.
- Refactor continuously, but only with tests protecting the behavior being changed.
- Prefer the simplest design that satisfies the current use case.
- Do not introduce speculative abstractions for requirements that do not yet exist.
- Treat code review, test feedback, and architecture constraints as continuous feedback loops.
- Bug fixes should aim to add a regression test first, then fix the code, then simplify if needed.
- When implementing new features, prefer thin vertical slices that deliver working behavior end to end.
- Preserve sustainable pace: avoid batching too many unrelated concerns into one change.
- Shared code ownership is expected; keep code understandable so another engineer can continue the work quickly.
- Respect existing module boundaries in the backend and the component architecture direction for the future Flutter app.

## Commit Convention (commitlint)
- Use Conventional Commits for all commit messages.
- Prefer format: `type(scope): short imperative summary`.
- Allowed `type` values (commitlint-config-conventional / Angular style):
  - `build`
  - `chore`
  - `ci`
  - `docs`
  - `feat`
  - `fix`
  - `perf`
  - `refactor`
  - `revert`
  - `style`
  - `test`
- Keep the subject concise, lower case (except proper nouns), and without trailing period.

## Other Agent/Tooling Rules
- Cursor rules: none found (`.cursor/rules/` and `.cursorrules` are absent).
- Copilot rules: none found (`.github/copilot-instructions.md` is absent).

## Security & Hygiene
- Never commit `.env` or credentials (see `.gitignore`; patterns include `*api-key*`, `*secret*`, `*token*`).
- Do not edit generated files in `build/`.
