---
type: technical-debt
id: TD-003
status: open
category: Tests
impact: 4
interest: high
effort: M

modules:
  - ai

areas:
  - test-configuration
  - integration-tests

causes:
  - environment-coupling
  - external-dependency

risks:
  - ci
  - reliability
  - cost

related:
  - AI Providers Integration Test
  - AIServicesIntegrationTest
  - application-test.properties
---

## Summary
Integration tests depend on local .env files and may call real LLM providers, which makes CI brittle and can incur costs.

## Evidence
- `src/test/java/dev/prospectos/integration/AIProvidersIntegrationTest.java` loads `file:.env` and enables `test` profile.
- `src/test/java/dev/prospectos/integration/AIServicesIntegrationTest.java` loads `file:.env` and uses live providers when keys exist.
- No `src/test/resources/application-test.properties` exists despite project guidance.

## Impact
- Tests fail in clean environments without `.env`.
- Test runs can hit real APIs, slowing down builds and creating cost surprises.

## Direction
- Introduce `src/test/resources/application-test.properties` with deterministic defaults and mock provider toggles.
- Gate live LLM calls behind an explicit profile or environment flag.
- Add a dedicated test double for LLM responses to keep CI offline.

## Links
- [[Testing Strategy]]
- [[AI Module]]
