# MVP-001 On-demand lead search API (Web/API module)

## Objective
Expose an HTTP endpoint that starts the on-demand lead search flow.

## Checklist
- [x] Create request DTO (query, limit, sources, optional ICP id).
- [x] Add validation annotations (query not blank, limit range 1-100).
- [x] Create response DTO (lead list + score or processing status).
- [x] Implement controller (POST /api/leads/search).
- [ ] Wire controller to orchestration service (MVP-006).
- [x] Add basic error mapping (400 for validation, 500 for unexpected).
- [x] Add minimal usage example in JavaDoc or controller comment.

## Acceptance criteria
- Endpoint accepts a valid request and returns a 200 with structured JSON.
- Invalid input returns 400 with clear error messages.
- Controller delegates to the flow service (no business logic in controller).
