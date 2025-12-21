---
type: technical-debt
id: TD-002
status: open
category: Build
impact: 3
interest: medium
effort: P

modules:
  - core
  - ai
  - infrastructure

areas:
  - build-system
  - dependency-versions

causes:
  - unstable-dependencies
  - snapshot-repos

risks:
  - build-reproducibility
  - deploy

related:
  - build.gradle
  - Spring Boot
  - Spring AI
---

## Summary
Build relies on snapshot and milestone dependencies, which makes builds less reproducible and increases the risk of breaking changes.

## Evidence
- `build.gradle` uses `org.springframework.boot` version `3.5.10-SNAPSHOT` and includes the Spring snapshot repo.
- `build.gradle` pins Spring AI BOM to `1.0.0-M4` and includes the Spring milestone repo.

## Impact
- CI and developer builds can break when upstream snapshots change.
- Dependency upgrades become harder to reason about and reproduce.

## Direction
- Move to stable Spring Boot and Spring AI releases when available.
- Remove snapshot and milestone repositories once versions are stable.
- Capture version policy in [[Build System]] notes.

## Links
- [[Build System]]
- [[Dependency Management]]
