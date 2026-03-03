# AI Configuration Troubleshooting Guide

## Quick Checks

1. Confirm `prospectos.ai.enabled`.
2. Confirm `prospectos.ai.active-providers`.
3. Confirm the relevant API key exists.
4. Confirm the active profile is not `test` unless mocks are expected.

## Common Issues

### AI services do not start

Typical causes:

- `prospectos.ai.enabled=false`
- no valid provider key configured
- provider not listed in `prospectos.ai.active-providers`
- Spring AI bootstrap beans disabled for the selected provider

Recommended baseline:

```properties
prospectos.ai.enabled=true
prospectos.ai.active-providers=groq,openai,anthropic
```

### Groq bean does not exist

Groq-specific beans are only created when:

- `groq` is included in `prospectos.ai.active-providers`
- `prospectos.ai.groq.api-key` is configured

Example:

```properties
prospectos.ai.active-providers=groq,openai
prospectos.ai.groq.api-key=${GROQ_API_KEY}
prospectos.ai.groq.base-url=https://api.groq.com/openai
```

### Test bootstrap fails because of AI beans

Use the test baseline:

```properties
prospectos.ai.enabled=false
prospectos.scoring.mock.enabled=true
spring.ai.vectorstore.type=none
scraper.ai.enabled=false
```

`LlmLeadDiscoverySource` now requires `AIProvider`, so it should not load when AI is disabled.

### Vector discovery confusion

There is no runtime `prospectos.discovery.vector.enabled` flag.

Use:

```properties
prospectos.leads.allowed-sources=in-memory,vector-company
```

### Property seems ignored

Check precedence:

1. command line
2. system properties
3. environment variables
4. `application-{profile}.properties`
5. `application.properties`

Useful env variables:

- `PROSPECTOS_AI_ACTIVE_PROVIDERS`
- `SPRING_AI_OPENAI_API_KEY`
- `SPRING_AI_ANTHROPIC_API_KEY`
- `PROSPECTOS_AI_GROQ_API_KEY`

Compatibility alias:

- `AI_PROVIDER_PRIORITY` -> `prospectos.ai.active-providers`
