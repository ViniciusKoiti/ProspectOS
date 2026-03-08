# Test Stabilization and Docker Alignment (2026-03-08)

## Context
This change set stabilizes local/integration test execution after recent AI configuration updates.
The objective was to make `./gradlew test` run consistently and align test behavior with the current AI configuration docs.

## What Changed

### 1) Docker and local Postgres setup
- Updated `docker-compose.yml` to use `pgvector/pgvector:pg15` (compatible with existing Postgres 15 data volume).
- Added Postgres healthcheck (`pg_isready`) for better startup readiness.
- Updated `init.sql` to ensure `vector` extension is created.

### 2) AI services integration test profile wiring
- Updated `AIServicesIntegrationTest` to run on `test` profile.
- Added explicit test properties:
  - `prospectos.ai.enabled=true`
  - `prospectos.ai.active-providers=mock`
  - `prospectos.scoring.mock.enabled=true`
  - `scraper.ai.enabled=false`
- Replaced field type from `ScoringAIService` to `ScoringService` to work with mock scoring in test profile.

### 3) PgVector integration test alignment
- Re-enabled `LeadDiscoveryVectorPgIntegrationTest` (removed temporary disabling).
- Removed obsolete discovery flag usage from the test setup.
- Kept PgVector store enabled in test properties and set `spring.ai.vectorstore.type=pgvector`.
- Removed in-test deterministic embedding class; centralized deterministic embedding in main test-support component.

### 4) Deterministic embedding model activation
- Added `TestDeterministicEmbeddingModel` as test-support embedding model.
- Activation now follows vector backend property:
  - active on `test` profile when `prospectos.vectorization.backend=pgvector`.

### 5) Prompt test support resources and tests
- Added test prompt resource files:
  - `src/test/resources/prompts/b2b-prospecting.txt`
  - `src/test/resources/prompts/scoring-system.txt`
- Added prompt-focused tests:
  - `AIPromptServiceIntegrationTest`
  - `AIPromptServiceSimpleTest`

## Validation Performed
- Executed `./gradlew test` with local Gradle user home and full suite.
- Final result observed: 358 tests, 0 failures, 0 errors, 0 skipped.

## Notes
- This update is aligned with the recent AI configuration direction where runtime gating is driven by:
  - `prospectos.ai.enabled`
  - `prospectos.ai.active-providers`
  - vector backend selection via `prospectos.vectorization.backend`
