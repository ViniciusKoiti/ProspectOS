# Schema Mismatch Cause

Responsibility: Incompatible ID or schema shapes between domain, persistence, and API layers.

Why this is expensive: Fixing mismatched IDs usually requires API changes, data migration, and coordinated updates across adapters.

## Related Technical Debts
- [[TD-001-mocked-core-data-service]]
