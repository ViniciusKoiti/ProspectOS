# MVP-005 AI scoring integration (AI module)

## Objective
Score leads using AI services and map results into the domain model.

## Checklist
- [ ] Use ScoringAIService to score a Company against ICP.
- [ ] Map AI output into Score and Priority.
- [ ] Handle AI failures with safe fallback (default score or error).
- [ ] Update company status based on score rules.
- [ ] Add unit tests for mapping and failure handling.

## Acceptance criteria
- Successful scoring populates Score with valid bounds.
- Failures do not persist partial or invalid data.
- Score category and status updates match domain rules.
