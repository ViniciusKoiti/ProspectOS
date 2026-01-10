# MVP-004 Enrichment and email validation (Core module)

## Objective
Normalize company/contact data and validate emails before scoring.

## Checklist
- [x] Define enrichment input/output contract (raw -> normalized).
- [x] Implement enrichment service (basic field cleanup + mapping).
- [x] Validate emails using Email value object.
- [x] Add domain validation (syntax + domain format).
- [x] Flag or remove invalid emails from contact list.
- [x] Add unit tests for enrichment and email validation.

## Acceptance criteria
- Invalid emails are filtered or flagged.
- Enriched data maps cleanly into Company and Contact.
- Enrichment is deterministic (no random data).
