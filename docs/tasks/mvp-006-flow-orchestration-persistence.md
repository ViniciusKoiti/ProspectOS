# MVP-006 Flow orchestration and persistence (Infrastructure module)

## Objective
Implement the end-to-end on-demand flow and persist results.

## Checklist
- [ ] Create orchestration service (OnDemandLeadSearchService).
- [ ] Execute steps: compliance -> scraping -> enrichment -> validation -> scoring -> persistence.
- [ ] Persist Company, contacts, and provenance.
- [ ] Build response DTOs for API layer.
- [ ] Add integration test for the happy path with mock AI.

## Acceptance criteria
- One request produces a persisted Company with score and provenance.
- Response includes leads with scores and sources.
- Errors in any step are returned cleanly without partial corruption.
