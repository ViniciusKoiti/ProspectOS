# Milestone MVP-1

## Objective
Deliver the end-to-end on-demand lead search flow with compliance, scraping, enrichment, scoring, and persistence.

## Scope
- Web/API entrypoint for on-demand lead search.
- Compliance registry for allowed sources and provenance tracking.
- External scraper integration with retries and errors.
- Enrichment + email validation in core domain.
- AI scoring mapped into domain rules.
- Orchestrated flow with persistence and integration coverage.

## Included tasks
- [MVP-001 On-demand lead search API](../tasks/mvp-001-on-demand-search-api.md)
- [MVP-002 Allowed sources compliance registry](../tasks/mvp-002-allowed-sources-compliance.md)
- [MVP-003 Scraper integration client](../tasks/mvp-003-scraper-integration-client.md)
- [MVP-004 Enrichment and email validation](../tasks/mvp-004-enrichment-email-validation.md)
- [MVP-005 AI scoring integration](../tasks/mvp-005-ai-scoring-integration.md)
- [MVP-006 Flow orchestration and persistence](../tasks/mvp-006-flow-orchestration-persistence.md)

## Acceptance criteria
- A single request produces a persisted Company with score and provenance.
- Responses contain leads with sources and AI-backed scoring or clear errors.
- Disallowed sources are rejected before scraping.
- Enrichment produces deterministic, validated contact data.
- AI scoring maps into Score/Priority with safe fallback on failure.
- Integration test covers the happy path with mock AI.

## Risks and dependencies
- External scraper availability and latency.
- AI provider limits and error handling.
- Persistence schema readiness for provenance and contacts.

## Status
- Owner: TBD
- Target date: TBD
- Current state: Planned
