
# AI Configuration Properties Reference

## Primary Properties

```properties
prospectos.ai.enabled=true
prospectos.ai.active-providers=openai,anthropic
```

`prospectos.ai.active-providers` is the application policy for provider order
and eligibility.

## Supported Provider Values

- `openai`
- `anthropic`
- `groq`
- `ollama`
- `mock`

## Provider Credential Properties

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

## Lead Discovery And Vectorization

```properties
prospectos.leads.allowed-sources=in-memory,vector-company,scraper,llm-discovery
prospectos.vectorization.backend=in-memory
prospectos.vectorization.model-id=hashing-v1
prospectos.vectorization.embedding-dimension=256
prospectos.vectorization.top-k=5
prospectos.vectorization.min-similarity=0.20
prospectos.vectorization.pgvector.table-name=company_vectors
prospectos.vectorization.pgvector.initialize-schema=true
```

`prospectos.leads.allowed-sources` is the single source of truth for discovery
source eligibility.

There is no `prospectos.discovery.vector.enabled` property in the current runtime model.

## Scraper

```properties
scraper.ai.enabled=true
scraper.ai.timeout=30s
scraper.ai.max-retries=2
scraper.ai.deep-search-enabled=false
scraper.ai.cache-timeout=1h
```

## Test Baseline

```properties
prospectos.ai.enabled=false
prospectos.scoring.mock.enabled=true
spring.ai.vectorstore.type=none
scraper.ai.enabled=false
```

## Environment Variables

Preferred:

- `SPRING_AI_OPENAI_API_KEY`
- `SPRING_AI_ANTHROPIC_API_KEY`
- `PROSPECTOS_AI_GROQ_API_KEY`
- `PROSPECTOS_AI_ACTIVE_PROVIDERS`

Compatible aliases:

- `OPENAI_API_KEY`
- `ANTHROPIC_API_KEY`
- `GROQ_API_KEY`
- `AI_PROVIDER_PRIORITY`

## Notes

- Application provider booleans like `prospectos.ai.groq.enabled` are obsolete.
- Spring provider bootstrap properties may still exist separately from the
  application provider policy.
