# Configuration Simplification

## Overview

Current configuration strategy:

- shared defaults in `application.properties`
- profile files only for overrides
- source activation by list/registry
- application AI provider policy by ordered list

## Discovery Sources

Before:

```properties
prospectos.leads.allowed-sources=in-memory,cnpj-ws,llm-discovery,vector-company
prospectos.discovery.llm.enabled=true
prospectos.discovery.vector.enabled=true
```

After:

```properties
prospectos.leads.allowed-sources=in-memory,cnpj-ws,llm-discovery,vector-company
```

`allowed-sources` is the single source of truth for discovery-source eligibility.

## AI Providers

Application provider policy is now centralized:

```properties
prospectos.ai.enabled=true
prospectos.ai.active-providers=openai,anthropic
```

This replaces app-level provider booleans as the main control plane.

## Shared Defaults

Shared defaults like these belong in `application.properties`:

- `scraper.ai.timeout`
- `scraper.ai.max-retries`
- `scraper.ai.deep-search-enabled`
- `scraper.ai.cache-timeout`
- `prospectos.vectorization.model-id`
- `prospectos.vectorization.embedding-dimension`
- `prospectos.vectorization.top-k`
- `prospectos.vectorization.min-similarity`

## Runtime Guardrails

- `LlmLeadDiscoverySource` only exists when `AIProvider` exists.
- test profile keeps `prospectos.ai.enabled=false`.
- vector discovery is controlled by `allowed-sources`, not by a second boolean.
