# MVP-002 Allowed sources compliance registry (Infrastructure module)

## Objective
Block disallowed sources and record provenance for every lead.

## Checklist
- [ ] Define allowed sources config (application properties or YAML).
- [ ] Create compliance service to validate requested sources.
- [ ] Add provenance model (source name, url, timestamp).
- [ ] Map provenance into result DTOs.
- [ ] Persist provenance with company or a related entity.
- [ ] Add unit tests for compliance checks.

## Acceptance criteria
- Requests using disallowed sources are rejected before scraping.
- Results include provenance for each lead.
- Allowed sources can be changed without code edits.
