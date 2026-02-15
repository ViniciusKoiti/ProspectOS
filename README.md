# ProspectOS

ProspectOS is a **Spring Boot + Spring Modulith** application for B2B prospecting workflows:

- discover leads
- enrich company signals
- score fit against ICPs (Ideal Customer Profiles)
- support outreach strategy decisions

The project is a modular monolith designed for teams that want fast iteration with explicit architectural boundaries.

## Why This Project Is Interesting For Spring Teams

- **Spring Modulith boundaries** validated by tests
- **Spring AI integration** with provider abstraction
- **Clear module contracts** (`api`) between domain and infrastructure
- **Profile-driven runtime behavior** (`mock`, `development`, `test`)
- **Pragmatic test strategy** (unit + integration)

## Architecture

Main packages/modules:

- `dev.prospectos.core`: domain model + business rules
- `dev.prospectos.api`: cross-module contracts and DTOs
- `dev.prospectos.ai`: LLM/provider integration and converters
- `dev.prospectos.infrastructure`: web, JPA adapters, scheduled jobs, integrations

Boundary verification lives in Modulith tests under `src/test/java/dev/prospectos/modules`.

## Tech Stack

- Java 21
- Spring Boot 3.5.x (snapshot)
- Spring Modulith 1.4.x
- Spring AI 1.0.0-M4
- Spring Data JPA
- H2 (dev/test)
- Gradle 8 (wrapper)
- JUnit 5, Mockito, AssertJ

## Quick Start

### Prerequisites

- JDK 21
- `JAVA_HOME` configured

### Run locally

```bash
./gradlew clean
./gradlew bootRun
```

Default profile is `mock` (safe local startup without real AI providers).

### Run tests

```bash
./gradlew test
```

Single test examples:

```bash
./gradlew test --tests 'dev.prospectos.core.domain.CompanyTest'
./gradlew test --tests 'dev.prospectos.integration.LeadDiscoveryIntegrationTest'
```

Coverage report:

- `build/reports/jacoco/test/html/index.html`

## Configuration Profiles

- `mock`: local-safe defaults, no real provider dependency
- `development`: local development with extended discovery setup
- `test`: deterministic integration tests with in-memory DB

`.env` loading is supported by `DotenvEnvironmentPostProcessor` and skipped in `test` profile.
Use `.env.example` as baseline and never commit secrets.

## API Surface (MVP)

- Companies
- `GET/POST /api/companies`
- `GET/PUT/DELETE /api/companies/{id}`
- `PUT /api/companies/{id}/score`

- ICPs
- `GET/POST /api/icps`
- `GET/PUT/DELETE /api/icps/{id}`
- `GET /api/icps/{id}/companies`

- Lead flows
- `POST /api/leads/discover`
- `POST /api/leads/accept`

- Enrichment
- `POST /api/prospect/enrich`

## Current Engineering Focus

Technical debt and execution history:

- `docs/technical-debt/README.md`
- `docs/technical-debt/TD-018-discovery-and-scoring-hardening.md`

Backlog and roadmap:

- `docs/tasks/index.md`
- `docs/implementation-roadmap.md`
- `docs/diagrams/README.md`

## Contributing

- Keep changes small and module-aware
- Respect Modulith boundaries (`core` must not depend on `ai`/`infrastructure`)
- Follow `.editorconfig`
- Add tests for behavioral changes
- Never commit `.env`, keys, or tokens
