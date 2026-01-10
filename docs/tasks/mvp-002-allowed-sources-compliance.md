# MVP-002 Allowed sources compliance registry (Infrastructure module)

## Objective
Block disallowed sources and record provenance for every lead.

## Checklist
- [x] Define allowed sources config (application properties or YAML).
- [x] Create compliance service to validate requested sources.
- [x] Add provenance model (source name, url, timestamp).
- [x] Map provenance into result DTOs.
- [x] Persist provenance with company or a related entity.
- [x] Add unit tests for compliance checks.

## Acceptance criteria
- Requests using disallowed sources are rejected before scraping.
- Results include provenance for each lead.
- Allowed sources can be changed without code edits.
