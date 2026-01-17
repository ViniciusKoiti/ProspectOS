# ProspectOS

ProspectOS is a Spring Boot (Java 21) application that helps with B2B prospecting: search leads, enrich company data, score fit against an ICP (Ideal Customer Profile), and generate strategy/outreach suggestions.

The codebase is organized as a Spring Modulith modular monolith with clear boundaries:
- `dev.prospectos.core`: domain + business rules
- `dev.prospectos.api`: service contracts + DTOs
- `dev.prospectos.ai`: AI/provider plumbing (Spring AI)
- `dev.prospectos.infrastructure`: web controllers, JPA adapters, jobs, integrations

## What You Can Do Today (MVP)
- Manage Companies (CRUD) and update scoring
- Manage ICPs (CRUD) and list companies by ICP
- Run lead search (on-demand)
- Enrich a prospect (basic flow)

Related endpoints (web layer):
- `GET/POST /api/companies`, `GET/PUT/DELETE /api/companies/{id}`, `PUT /api/companies/{id}/score`
- `GET/POST /api/icps`, `GET/PUT/DELETE /api/icps/{id}`, `GET /api/icps/{id}/companies`
- `POST /api/leads/search`
- `POST /api/prospect/enrich`

## Local Setup

### Requirements
- JDK 21 (Gradle uses a Java 21 toolchain)
- Use the Gradle wrapper shipped with the repo: `./gradlew`

If you see errors like "JAVA_HOME is not set", install JDK 21 and set `JAVA_HOME`.

### Profiles + .env
- Default profile is `mock` (`src/main/resources/application.properties`)
- `.env` is supported via `DotenvEnvironmentPostProcessor` (skipped when `test` profile is active)
- Create your local env file from `.env.example` (never commit `.env`)

Preferred env vars for AI providers:
- `SPRING_AI_OPENAI_API_KEY`
- `SPRING_AI_ANTHROPIC_API_KEY`
- `PROSPECTOS_AI_GROQ_API_KEY`

### Common Commands
```bash
./gradlew clean
./gradlew build
./gradlew test
./gradlew bootRun
```

Run a single test (most important):
```bash
./gradlew test --tests 'dev.prospectos.core.domain.CompanyTest'
./gradlew test --tests 'dev.prospectos.core.domain.CompanyTest.someTestMethod'
```

Coverage:
- `./gradlew test` produces JaCoCo output at `build/reports/jacoco/test/html/index.html`

## CI
A GitHub Actions workflow lives at `.github/workflows/ci.yml` and runs tests on:
- Pull Requests targeting `main`
- Pushes to `main`

Recommended: enable Branch Protection on `main` requiring the CI check to pass.

## Current Pending Work (Known Gaps)

High priority tech debt (see `docs/technical-debt/README.md`):
- TD-002 (CRITICAL): `.env` is currently present in the repository. It should be removed from git and any leaked secrets must be rotated.
- TD-001 (CRITICAL): build uses SNAPSHOT/Milestone dependencies (Spring Boot/Spring AI). Migrate to stable versions when possible.

Product/MVP backlog (see `docs/tasks/index.md` and diagrams under `docs/diagrams/`):
- MVP-004: enrichment + email validation improvements (in progress)
- MVP-006: flow orchestration + persistence improvements (pending)

## Docs
- MVP backlog index: `docs/tasks/index.md`
- Implementation roadmap: `docs/implementation-roadmap.md`
- Diagrams (PlantUML): `docs/diagrams/README.md`

## Notes For Contributors
- Follow `.editorconfig` (4 spaces for Java/Gradle/properties, max line length 120)
- Keep diffs focused and respect Modulith boundaries (core must not depend on ai/infrastructure)
- Never commit secrets (.env, tokens, api keys)
