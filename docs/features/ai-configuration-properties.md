# AI Configuration Properties Reference

## Complete Properties List

### Master Controls

#### Global AI Toggle
```properties
# Enable/disable all AI features globally
prospectos.ai.enabled=true
```
**Default**: `true`  
**Profiles**: Disabled in `test` profile  
**Description**: Master switch for all AI functionality. When `false`, all AI services are disabled and fallback to mock implementations.

### Provider-Specific Configuration

#### Groq Configuration
```properties
# Enable Groq provider
prospectos.ai.groq.enabled=true

# Groq API credentials
prospectos.ai.groq.api-key=${GROQ_API_KEY}

# Groq API endpoint
prospectos.ai.groq.base-url=https://api.groq.com/openai

# Model selection
prospectos.ai.groq.model=llama3-8b-8192
```

**Groq Model Options**:
- `llama3-8b-8192` - Fast, cost-effective
- `llama3-70b-8192` - Higher quality, slower
- `mixtral-8x7b-32768` - Large context window

#### OpenAI Configuration
```properties
# OpenAI API credentials
spring.ai.openai.api-key=${OPENAI_API_KEY}

# Model configuration
spring.ai.openai.chat.options.model=gpt-4-turbo-preview
spring.ai.openai.chat.options.temperature=0.7
spring.ai.openai.chat.options.max-tokens=1000

# Embedding model
spring.ai.openai.embedding.options.model=text-embedding-3-small
```

**OpenAI Model Options**:
- `gpt-4-turbo-preview` - Best quality, expensive
- `gpt-3.5-turbo` - Good balance of cost/quality
- `gpt-4o` - Latest multimodal model

#### Anthropic Configuration
```properties
# Anthropic API credentials
spring.ai.anthropic.api-key=${ANTHROPIC_API_KEY}

# Model configuration
spring.ai.anthropic.chat.options.model=claude-3-5-sonnet-20241022
spring.ai.anthropic.chat.options.temperature=0.7
spring.ai.anthropic.chat.options.max-tokens=1000
```

**Anthropic Model Options**:
- `claude-3-5-sonnet-20241022` - Best for analysis
- `claude-3-haiku-20240307` - Fast and economical
- `claude-3-opus-20240229` - Highest intelligence

### Discovery & Vectorization

#### Discovery Configuration
```properties
# Enable LLM-based discovery
prospectos.discovery.llm.enabled=true

# Vector-based discovery
prospectos.discovery.vector.enabled=true

# Lead source configuration
prospectos.leads.allowed-sources=in-memory,vector-company,scraper,llm-discovery
```

**Lead Source Options**:
- `in-memory` - Predefined companies in memory
- `vector-company` - Vector similarity search
- `scraper` - Web scraping integration
- `llm-discovery` - AI-powered company discovery

#### Vectorization Settings
```properties
# Vector backend selection
prospectos.vectorization.backend=pgvector

# Model configuration
prospectos.vectorization.model-id=hashing-v1
prospectos.vectorization.embedding-dimension=256

# Search parameters
prospectos.vectorization.top-k=5
prospectos.vectorization.min-similarity=0.20

# PGVector specific
prospectos.vectorization.pgvector.enabled=true
prospectos.vectorization.pgvector.initialize-schema=true
```

**Vector Backend Options**:
- `in-memory` - Fast, development-friendly
- `pgvector` - Production PostgreSQL with vector extensions

### Service-Specific Settings

#### Scoring Configuration
```properties
# Enable AI scoring
prospectos.scoring.enabled=true

# Scoring thresholds
prospectos.scoring.hot-threshold=80
prospectos.scoring.warm-threshold=60
prospectos.scoring.cold-threshold=40
```

#### Scraping Integration
```properties
# Web scraping configuration
scraper.enabled=true
scraper.ai.enabled=true
scraper.ai.cache-timeout=1h
scraper.python-service.url=http://localhost:8001
```

### Profile-Specific Configurations

#### Development Profile
```properties
# Profile: development
spring.profiles.active=development

# AI Configuration
prospectos.ai.enabled=true
prospectos.ai.groq.enabled=true
prospectos.discovery.llm.enabled=true

# Lead Sources (all sources enabled)
prospectos.leads.allowed-sources=in-memory,scraper,llm-discovery,vector-company

# Vector Store
prospectos.vectorization.backend=in-memory

# Database
spring.datasource.url=jdbc:h2:mem:devdb
```

#### Production Profile
```properties
# Profile: production
spring.profiles.active=production

# AI Configuration
prospectos.ai.enabled=true
prospectos.ai.groq.enabled=true

# Lead Sources (stable sources only)
prospectos.leads.allowed-sources=in-memory,vector-company

# Vector Store
prospectos.vectorization.backend=pgvector

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/prospectos_prod
```

#### Test Profile
```properties
# Profile: test
spring.profiles.active=test

# AI Disabled for deterministic tests
prospectos.ai.enabled=false

# Vector Store
spring.ai.vectorstore.type=none
prospectos.vectorization.backend=in-memory
prospectos.vectorization.pgvector.enabled=false

# Lead Sources (minimal for testing)
prospectos.leads.allowed-sources=in-memory,vector-company

# Database
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop
```

#### Mock Profile
```properties
# Profile: mock (default)
spring.profiles.active=mock

# AI Configuration (disabled for local dev)
prospectos.ai.enabled=false

# Lead Sources
prospectos.leads.allowed-sources=in-memory,vector-company

# Vector Store
prospectos.vectorization.backend=in-memory

# Database
spring.datasource.url=jdbc:h2:mem:mockdb
```

## Environment Variables

### Required Environment Variables

#### Production
```bash
# AI Provider API Keys
GROQ_API_KEY=gsk_...
OPENAI_API_KEY=sk-...
ANTHROPIC_API_KEY=sk-ant-...

# Database
POSTGRES_URL=jdbc:postgresql://localhost:5432/prospectos
POSTGRES_USERNAME=prospectos_user
POSTGRES_PASSWORD=secure_password

# Vector Store
PGVECTOR_ENABLED=true
```

#### Development
```bash
# AI Provider API Keys (at least one required)
GROQ_API_KEY=gsk_...

# Optional: Additional providers
OPENAI_API_KEY=sk-...
ANTHROPIC_API_KEY=sk-ant-...

# Development toggles
PROSPECTOS_AI_ENABLED=true
PROSPECTOS_DISCOVERY_LLM_ENABLED=true
```

#### Test/CI
```bash
# Minimal configuration for tests
PROSPECTOS_AI_ENABLED=false
SPRING_PROFILES_ACTIVE=test
```

## Property Validation

### Required Properties by Profile

#### Development Profile Requirements
- `GROQ_API_KEY` (if `prospectos.ai.groq.enabled=true`)
- `prospectos.ai.enabled=true`
- `prospectos.leads.default-icp-id` (numeric)

#### Production Profile Requirements
- At least one AI provider API key
- `prospectos.vectorization.backend=pgvector`
- Database connection properties
- `prospectos.ai.enabled=true`

#### Test Profile Requirements
- `prospectos.ai.enabled=false`
- `spring.ai.vectorstore.type=none`
- `spring.jpa.hibernate.ddl-auto=create-drop`

### Property Validation Rules

#### API Key Validation
```java
@NotBlank(message = "API key cannot be empty")
@Pattern(regexp = "^(sk-|gsk_|sk-ant-).*", message = "Invalid API key format")
private String apiKey;
```

#### Numeric Properties
```properties
# Must be positive integers
prospectos.leads.default-icp-id=1
prospectos.vectorization.top-k=5
prospectos.vectorization.embedding-dimension=256

# Must be between 0.0 and 1.0
prospectos.vectorization.min-similarity=0.20
```

#### Boolean Properties
```properties
# Explicit boolean values recommended
prospectos.ai.enabled=true
prospectos.ai.groq.enabled=false
prospectos.discovery.llm.enabled=true
```

## Configuration Priority

### Property Source Priority (highest to lowest)
1. Command line arguments (`--prospectos.ai.enabled=true`)
2. Java system properties (`-Dprospectos.ai.enabled=true`)
3. OS environment variables (`PROSPECTOS_AI_ENABLED=true`)
4. Profile-specific files (`application-{profile}.properties`)
5. Default configuration (`application.properties`)

### Profile Activation Priority
1. `spring.profiles.active` system property
2. `SPRING_PROFILES_ACTIVE` environment variable
3. Default profile (`mock`)

## Common Configuration Patterns

### Local Development
```properties
# .env file (gitignored)
GROQ_API_KEY=gsk_your_development_key
SPRING_PROFILES_ACTIVE=development
PROSPECTOS_AI_ENABLED=true
PROSPECTOS_DISCOVERY_LLM_ENABLED=false
```

### Docker Compose
```yaml
environment:
  - SPRING_PROFILES_ACTIVE=production
  - GROQ_API_KEY=${GROQ_API_KEY}
  - POSTGRES_URL=jdbc:postgresql://db:5432/prospectos
  - PROSPECTOS_AI_ENABLED=true
  - PROSPECTOS_VECTORIZATION_BACKEND=pgvector
```

### Kubernetes ConfigMap
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: prospectos-config
data:
  application.properties: |
    spring.profiles.active=production
    prospectos.ai.enabled=true
    prospectos.vectorization.backend=pgvector
    prospectos.leads.allowed-sources=in-memory,vector-company
```

## Configuration Troubleshooting

### Debug Configuration Loading
```properties
# Enable configuration debugging
logging.level.org.springframework.boot.autoconfigure=DEBUG
logging.level.org.springframework.boot.context.config=DEBUG

# Show property resolution
debug=true
```

### Validate Configuration
```bash
# Check active profile
curl http://localhost:8080/actuator/env | jq '.activeProfiles'

# Check specific property
curl http://localhost:8080/actuator/env/prospectos.ai.enabled

# Check all AI-related properties
curl http://localhost:8080/actuator/configprops | jq '.contexts.application.beans | to_entries[] | select(.key | contains("ai"))'
```

### Common Validation Errors
```
Error: Property 'prospectos.ai.groq.api-key' must not be empty
Fix: Set GROQ_API_KEY environment variable

Error: No qualifying bean of type 'ChatModel' found
Fix: Enable at least one AI provider with valid API key

Error: ConditionalOnProperty condition was not met
Fix: Check property spelling and boolean values (true/false)
```