# MVP-003 Scraper integration client (Infrastructure module)

## Objective
Implement live scraping via the external scraper service.

## Checklist
- [ ] Replace stub in ScraperClient with HTTP integration.
- [ ] Use SCRAPER_SERVICE_URL and SCRAPER_SERVICE_TIMEOUT configs.
- [ ] Define request/response mapping for scraped content.
- [ ] Add retry/backoff for transient failures.
- [ ] Return actionable error messages on failure.
- [ ] Add unit tests for mapping and error handling.

## Acceptance criteria
- Scraper client calls succeed with configured URL and timeout.
- Response includes content needed for enrichment.
- Failures are logged and surfaced without crashing the flow.
