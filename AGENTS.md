
# Repository Guidelines

## Project Structure & Module Organization
- `src/main/java/dev/prospectos` holds the Spring Boot app and modules: `core` (domain + APIs), `ai` (LLM clients/services), and `infrastructure` (JPA adapters, repositories, services).
- `src/main/resources` contains Spring configuration such as `application.properties` and `application-mock.properties`.
- `src/test/java/dev/prospectos` contains JUnit tests; integration tests live under `integration`.
- `docs/` includes architecture diagrams and design notes used by the team.
- `build/` is Gradle output and should not be edited directly.

## Build, Test, and Development Commands
- `./gradlew build` builds the project and runs tests.
- `./gradlew bootRun` starts the application locally.
- `./gradlew test` runs the full test suite.
- `./gradlew test --tests "*ModulithTest"` runs modulith boundary checks only.
- `./gradlew clean` removes build artifacts.

## Coding Style & Naming Conventions
- Java 21, standard Spring Boot conventions; keep indentation at 4 spaces.
- Packages use `dev.prospectos.<module>`; classes `PascalCase`, methods/fields `camelCase`, constants `UPPER_SNAKE_CASE`.
- Module boundaries are enforced via Spring Modulith; keep `core` dependency-free from other modules.
- No formatter is enforced in Gradle; rely on IDE formatting and keep changes consistent with existing files.

## Testing Guidelines
- Frameworks: JUnit 5, Spring Boot Test, Spring Modulith Test, Mockito.
- Test naming uses `*Test` and `*IntegrationTest` patterns.
- Tests expect `.env` for AI keys when running live integrations; see `OPENAI_API_KEY` and `ANTHROPIC_API_KEY`.

## Commit & Pull Request Guidelines
- Recent commits use conventional prefixes like `feat:`, `refactor:`, `test:`; follow this style where possible.
- PRs should include a short summary, testing notes (commands + results), and linked issues.
- If you change API behavior, AI prompts, or module boundaries, update relevant docs in `docs/`.

## Configuration & Secrets
- Local AI keys are read from `.env` via the Dotenv post-processor.
- Use `src/test/resources/application-test.properties` for test-specific overrides.
