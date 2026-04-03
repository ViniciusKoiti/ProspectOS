# Apify, PageSpeed, and Hunter Implementation Plan

## Objective
Define the next production slices for the lead pipeline after `google-places` and source recommendation:

1. Apify as fallback discovery source
2. Google PageSpeed Insights as technical website audit
3. Hunter as contact enrichment by domain

This document is intentionally implementation-oriented and commit-oriented.

## Scope Decision
The internal demo site generator is out of the active roadmap for now.

Reason:
- it is not required to validate the commercial value of the lead pipeline
- discovery, qualification, and contact enrichment are higher leverage
- human-assisted proposal generation is acceptable at this stage

## Delivery Order
1. Apify
2. PageSpeed
3. Hunter

This order is intentional:
- Apify strengthens discovery and reduces dependency on a single source
- PageSpeed improves lead qualification quality
- Hunter converts discovered companies into actionable contacts

## Slice 1: Apify Discovery Source

### Goal
Add `apify` as a production-grade `LeadDiscoverySource` that can be used explicitly and later recommended as a fallback when `google-places` underperforms.

### Architecture
New production classes:
- `ApifyLeadDiscoverySource`
- `ApifyProperties`
- `ApifyRunRequest`
- `ApifyRunResponse`
- `ApifyDatasetItem`
- `ApifyResponseMapper`

Current integration point:
- `LeadDiscoverySource`
- `DefaultLeadDiscoveryService`
- `DiscoverySourceRegistry`

### Behavioral Contract
Input:
- free-text query
- limit

Output:
- `DiscoveredLeadCandidate`

Expected mapped fields:
- name
- website
- location
- phone
- industry or category
- sourceName=`apify`

### Operational Rules
- source must be disabled by default
- API token must be required when enabled
- failures should not be silently promoted to valid data
- logs should clearly indicate actor/run failure

### Initial Implementation Strategy
Prefer a simple synchronous or polling-based API flow:
1. trigger actor run
2. wait for completion or poll the run status
3. fetch dataset items
4. map items to `DiscoveredLeadCandidate`

### Tests
Unit:
- mapper tests for item-to-candidate conversion
- property normalization tests
- source tests for enabled/disabled behavior
- source tests for non-2xx and empty dataset behavior

Integration:
- `DefaultLeadDiscoveryServiceTest` updated with `apify`
- recommendation tests updated when `apify` becomes an eligible source

### Config
New env/property family:
- `PROSPECTOS_LEADS_APIFY_ENABLED`
- `PROSPECTOS_LEADS_APIFY_API_TOKEN`
- `PROSPECTOS_LEADS_APIFY_ACTOR_ID`
- `PROSPECTOS_LEADS_APIFY_BASE_URL`
- `PROSPECTOS_LEADS_APIFY_TIMEOUT`

### Commit Plan
1. `docs(discovery): add apify implementation plan`
2. `feat(discovery): add apify source properties and contracts`
3. `feat(discovery): add apify lead discovery source`
4. `test(discovery): cover apify source mapping and failures`
5. `feat(search): add apify to recommendation priority and allowed sources`

## Slice 2: Google PageSpeed Audit

### Goal
Add a real website technical audit provider that complements the internal heuristic auditor already present in prospect enrichment.

### Architecture
New production classes:
- `WebsiteAuditProvider`
- `GooglePageSpeedAuditProvider`
- `PageSpeedProperties`
- `PageSpeedResponse`
- `PageSpeedAuditMapper`

Current integration point:
- `ProspectEnrichmentFacade`
- `ProspectEnrichResponse`
- `ProspectWebsiteAuditResponse`

### Behavioral Contract
Input:
- website URL

Output:
- performance score
- mobile or usability indicators
- findings suitable for sales qualification

### Integration Strategy
Keep the current internal auditor as the baseline heuristic.

Then:
1. run internal audit
2. call PageSpeed
3. enrich the audit response with technical evidence

This keeps the system useful even if PageSpeed is disabled or rate-limited.

### Tests
Unit:
- mapper tests from PageSpeed payload to internal audit model
- provider tests for enabled/disabled and invalid response

Integration:
- `ProspectEnrichmentFacadeIntegrationTest`
- `ProspectEnrichmentIntegrationTest` when Docker/Testcontainers is available

### Config
New env/property family:
- `PROSPECTOS_PROSPECT_PAGESPEED_ENABLED`
- `PROSPECTOS_PROSPECT_PAGESPEED_API_KEY`
- `PROSPECTOS_PROSPECT_PAGESPEED_STRATEGY`

### Commit Plan
1. `docs(prospect): add pagespeed audit plan`
2. `feat(prospect): add pagespeed audit provider contracts`
3. `feat(prospect): integrate pagespeed into prospect enrichment`
4. `test(prospect): cover pagespeed provider and enrichment flow`

## Slice 3: Hunter Contact Enrichment

### Goal
Add domain-based contact enrichment so that discovered companies become actionable leads.

### Architecture
New production classes:
- `ContactEnrichmentProvider`
- `HunterContactEnrichmentProvider`
- `HunterProperties`
- `HunterDomainSearchResponse`
- `HunterContactMapper`

Current integration point:
- `ProspectEnrichmentFacade`
- contact processing already present via `ContactProcessor`

### Behavioral Contract
Input:
- company website or domain

Output:
- validated contact candidates
- source attribution to `hunter`

### Integration Strategy
1. normalize website and extract domain
2. call Hunter domain search
3. map contacts
4. pass them through existing contact processing and prioritization

### Tests
Unit:
- domain extraction tests
- Hunter mapper tests
- provider tests for empty and error responses

Integration:
- `ProspectEnrichmentFacadeIntegrationTest`
- `LeadAcceptIntegrationTest` only if enriched contacts are later persisted into accepted leads

### Config
New env/property family:
- `PROSPECTOS_PROSPECT_HUNTER_ENABLED`
- `PROSPECTOS_PROSPECT_HUNTER_API_KEY`
- `PROSPECTOS_PROSPECT_HUNTER_BASE_URL`

### Commit Plan
1. `docs(prospect): add hunter enrichment plan`
2. `feat(prospect): add hunter contact enrichment contracts`
3. `feat(prospect): integrate hunter contacts into prospect enrichment`
4. `test(prospect): cover hunter provider and contact enrichment flow`

## Cross-Cutting Rules
- Do not introduce new capability profiles for these integrations
- Enable by property, not by profile
- Keep all adapters production-ready by default
- Add direct tests for new DTOs, records, and mappers
- Use targeted integration tests instead of broad full-suite runs while the slice is evolving

## Recommended PR Strategy
Do not bundle Apify, PageSpeed, and Hunter into one giant PR.

Preferred sequence:
1. PR 1: Apify discovery source
2. PR 2: PageSpeed audit provider
3. PR 3: Hunter contact enrichment

This keeps review load reasonable and isolates risk by integration.

## Suggested Immediate Next Step
Start with Apify only.

Reason:
- it strengthens the current discovery system directly
- it improves the value of source recommendation
- it does not force immediate change to the enrichment contract
