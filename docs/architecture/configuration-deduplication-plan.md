# Configuration Deduplication Plan

## Objective

Reduce configuration duplication and keep a single source of truth for:

- shared defaults
- discovery source activation
- AI provider policy

## Implemented In This Branch

### Scraper defaults

Shared `scraper.ai.*` defaults were consolidated into `application.properties`.
Profile files now keep only explicit overrides such as `scraper.ai.enabled`.

### Vectorization defaults

Duplicated `prospectos.vectorization.*` defaults were reduced so common values
live in `application.properties` and profile files only override backend or
profile-specific behavior.

### Discovery source flags

`prospectos.discovery.vector.enabled` was removed from the active runtime path.
Discovery source eligibility is controlled by:

```properties
prospectos.leads.allowed-sources=...
```

### AI provider policy

Application provider selection is now centralized in:

```properties
prospectos.ai.active-providers=openai,anthropic
```

This replaces app-level provider booleans as the main control plane.

### Bootstrap safety

`LlmLeadDiscoverySource` is only created when an `AIProvider` bean exists. This
avoids test bootstrap failures when `prospectos.ai.enabled=false`.

## Current State

### Single sources of truth

- discovery sources: `prospectos.leads.allowed-sources`
- provider policy: `prospectos.ai.active-providers`
- shared scraper defaults: `application.properties`
- shared vectorization defaults: `application.properties`

### Remaining nuances

- Spring-specific properties such as `spring.ai.openai.enabled` and
  `spring.ai.anthropic.enabled` may still exist in profile files because they
  affect Spring AI autoconfiguration, not application provider policy.
- Older architecture/refactoring documents may still mention the previous model.

## Recommended Next Steps

1. Continue aligning historical docs under `docs/refactoring` and `docs/features`.
2. Keep new config additions in `application.properties` unless they are true profile overrides.
3. Prefer registries/lists over duplicated booleans for future feature activation.
