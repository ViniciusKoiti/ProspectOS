# AI Configuration Feature

## Overview

ProspectOS uses a two-level AI configuration model:

- `prospectos.ai.enabled` enables or disables the application AI module
- `prospectos.ai.active-providers` defines which providers may be selected and in which order

Provider selection is completed at runtime based on valid keys and available beans.

## Core Properties

```properties
prospectos.ai.enabled=true
prospectos.ai.active-providers=openai,anthropic
```

Supported provider names:

- `openai`
- `anthropic`
- `groq`
- `ollama`
- `mock`

## Provider Credentials

### OpenAI

```properties
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.openai.chat.options.model=gpt-4-turbo-preview
```

### Anthropic

```properties
spring.ai.anthropic.api-key=${ANTHROPIC_API_KEY}
spring.ai.anthropic.chat.options.model=claude-3-5-sonnet-20241022
```

### Groq

```properties
prospectos.ai.groq.api-key=${GROQ_API_KEY}
prospectos.ai.groq.base-url=https://api.groq.com/openai
prospectos.ai.groq.model=llama3-8b-8192
```

## Profiles

### Development

```properties
prospectos.ai.enabled=true
prospectos.ai.active-providers=openai,anthropic
prospectos.leads.allowed-sources=in-memory,scraper,llm-discovery,vector-company,cnpj-ws
prospectos.vectorization.backend=in-memory
scraper.ai.enabled=true
```

### Production

```properties
prospectos.ai.enabled=true
prospectos.ai.active-providers=openai,anthropic
prospectos.leads.allowed-sources=in-memory,vector-company
prospectos.vectorization.backend=pgvector
scraper.ai.enabled=${SCRAPER_AI_ENABLED:true}
```

### Test

```properties
prospectos.ai.enabled=false
prospectos.scoring.mock.enabled=true
spring.ai.vectorstore.type=none
scraper.ai.enabled=false
```

## Discovery Integration

Discovery source eligibility is controlled by `prospectos.leads.allowed-sources`.
There is no `prospectos.discovery.vector.enabled` runtime flag.

`LlmLeadDiscoverySource` is only created when an `AIProvider` bean exists, which
prevents test bootstrap failures when AI is disabled.

## Environment Variables

Preferred:

- `SPRING_AI_OPENAI_API_KEY`
- `SPRING_AI_ANTHROPIC_API_KEY`
- `PROSPECTOS_AI_GROQ_API_KEY`
- `PROSPECTOS_AI_ACTIVE_PROVIDERS`

Backward-compatible aliases:

- `OPENAI_API_KEY`
- `ANTHROPIC_API_KEY`
- `GROQ_API_KEY`
- `AI_PROVIDER_PRIORITY`

`AI_PROVIDER_PRIORITY` is mapped to `prospectos.ai.active-providers`.

## Notes

- App-level booleans such as `prospectos.ai.groq.enabled` are obsolete.
- Spring properties like `spring.ai.openai.enabled` can still appear in profile
  files when needed for Spring AI autoconfiguration behavior.
