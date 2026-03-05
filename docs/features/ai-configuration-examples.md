# AI Configuration Usage Examples

## Local Development

```bash
SPRING_PROFILES_ACTIVE=development
PROSPECTOS_AI_ACTIVE_PROVIDERS=groq,openai,anthropic
PROSPECTOS_AI_GROQ_API_KEY=gsk_your_groq_key
SPRING_AI_OPENAI_API_KEY=sk_your_openai_key
SPRING_AI_ANTHROPIC_API_KEY=sk-ant-your_anthropic_key
```

## Production

```properties
prospectos.ai.enabled=true
prospectos.ai.active-providers=openai,anthropic
prospectos.leads.allowed-sources=in-memory,vector-company
prospectos.vectorization.backend=pgvector
scraper.ai.enabled=${SCRAPER_AI_ENABLED:true}
```

## Test

```properties
prospectos.ai.enabled=false
prospectos.scoring.mock.enabled=true
spring.ai.vectorstore.type=none
scraper.ai.enabled=false
prospectos.leads.allowed-sources=in-memory,vector-company
```

## Groq-First Policy

```properties
prospectos.ai.enabled=true
prospectos.ai.active-providers=groq,openai
prospectos.ai.groq.api-key=${GROQ_API_KEY}
prospectos.ai.groq.base-url=https://api.groq.com/openai
prospectos.ai.groq.model=llama3-8b-8192
```

## OpenAI-Only Policy

```properties
prospectos.ai.enabled=true
prospectos.ai.active-providers=openai
spring.ai.openai.api-key=${OPENAI_API_KEY}
```

## Anthropic-Only Policy

```properties
prospectos.ai.enabled=true
prospectos.ai.active-providers=anthropic
spring.ai.anthropic.api-key=${ANTHROPIC_API_KEY}
```

## Disable LLM Discovery But Keep Vector Discovery

```properties
prospectos.leads.allowed-sources=in-memory,vector-company
prospectos.vectorization.backend=in-memory
```

## Compatibility Alias

```bash
AI_PROVIDER_PRIORITY=groq,openai
```

Maps to:

```properties
prospectos.ai.active-providers=groq,openai
```
