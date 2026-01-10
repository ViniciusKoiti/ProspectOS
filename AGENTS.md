# Repository Guidelines

## Project Structure & Module Organization
Source lives in `src/main/java/dev/prospectos` with submodules: `core` (domain models and APIs), `ai` (LLM client plumbing), and `infrastructure` (JPA adapters and services). Spring configs sit in `src/main/resources` (`application.properties`, `application-mock.properties`). Tests live in `src/test/java/dev/prospectos`, with integration tests tucked under `integration`. Shared docs and diagrams are tracked in `docs/`, while Gradle output stays inside `build/` and should remain untouched.

## Build, Test, and Development Commands
Run `./gradlew build` for a full compile plus the test suite. Use `./gradlew test` to execute the standard JUnit set, or narrow to modulith boundaries with `./gradlew test --tests "*ModulithTest"`. Start the app locally via `./gradlew bootRun`, and clear derived artifacts with `./gradlew clean` before a fresh build.

## Coding Style & Naming Conventions
Code targets Java 21 with standard Spring idioms and 4-space indentation. Follow package naming `dev.prospectos.<module>`, PascalCase for classes, camelCase for members, and upper snake case for constants. `core` must stay dependency-free from other modules to satisfy Modulith rules. No formatter is enforced, so rely on IDE defaults and keep diffs minimal.

## Testing Guidelines
Tests use JUnit 5, Spring Boot Test, Spring Modulith Test, and Mockito. Name classes with `*Test` or `*IntegrationTest`. Place integration scenarios under `src/test/java/.../integration` and apply `application-test.properties` for overrides. Provide `.env` entries for `OPENAI_API_KEY` and `ANTHROPIC_API_KEY` when running AI integrations.

## Commit & Pull Request Guidelines
Commits follow conventional prefixes (e.g., `feat:`, `refactor:`, `test:`) with concise scopes. PRs should summarize the change, document test commands/results, and reference linked issues. Update `docs/` whenever API behavior, AI prompts, or module boundaries shift.

## Security & Configuration Tips
Secrets flow through a Dotenv post-processor; never hardcode keys. Store only safe defaults in tracked configs and rely on `.env` for sensitive values. Keep module boundaries in mind when wiring new services so that `core` remains isolated from infrastructure-specific dependencies.
