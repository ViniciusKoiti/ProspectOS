---
type: technical-debt
id: TD-001
status: open
category: Data
impact: 5
interest: high
effort: G

modules:
  - core
  - infrastructure

areas:
  - data-access
  - dto-mapping

causes:
  - temporary-mock
  - schema-mismatch

risks:
  - production
  - reliability
  - data-quality

related:
  - Core Data Service
  - Company Repository Adapter
  - ICP Repository Adapter
---

## Summary
Core data API returns mock DTOs and uses lossy UUID to Long conversions, so API consumers cannot access real persisted data.

## Evidence
- `src/main/java/dev/prospectos/infrastructure/service/CoreDataServiceImpl.java` returns `CompanyDTO.createMock()` and `ICPDto.createMock()` for read operations.
- `src/main/java/dev/prospectos/infrastructure/service/CoreDataServiceImpl.java` converts UUID to Long via `getMostSignificantBits()` in `toDTO`.
- `src/main/java/dev/prospectos/infrastructure/service/CoreDataServiceImpl.java` keeps `companyRepository` and `icpRepository` unused.

## Impact
- External modules see fake data, blocking real workflows.
- ID conversions are not stable across systems and can collide or break references.

## Direction
- Implement repository-backed reads and updates.
- Use UUID in API DTOs or introduce a stable external ID mapping layer.
- Add tests that validate the real persistence path.

## Links
- [[Temporary Mock Cause]]
- [[Schema Mismatch Cause]]
- [[Data Access Area]]
- [[DTO Mapping Area]]
- [[Core Module]]
- [[Infrastructure Module]]
