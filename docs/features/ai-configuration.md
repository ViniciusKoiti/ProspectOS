# AI Configuration Feature

## Overview

The AI Configuration feature provides flexible, profile-aware configuration for multiple AI providers in the ProspectOS application. This system supports OpenAI, Anthropic Claude, and Groq providers with automatic fallback mechanisms and environment-specific configurations.

## Architecture

### Core Components

#### Configuration Classes
- **SpringAIConfig**: Central configuration orchestrator
- **GroqChatModelConfig**: Groq chat model configuration with OpenAI-compatible API
- **GroqEmbeddingConfig**: Groq embedding model configuration
- **AIProvider Interface**: Abstraction for different AI providers

#### Service Layer
- **ProspectorAIService**: Main AI service for prospecting decisions
- **ScoringAIService**: Intelligent scoring with structured output
- **OutreachAIService**: Personalized message generation
- **StrategyAIService**: Outreach strategy recommendations

## Configuration Properties

### Core AI Settings
```properties
# Master AI toggle
prospectos.ai.enabled=true

# Provider-specific toggles
prospectos.ai.groq.enabled=true
prospectos.ai.openai.enabled=true
prospectos.ai.anthropic.enabled=true
```

### Groq Configuration
```properties
# Groq API Configuration
prospectos.ai.groq.api-key=${GROQ_API_KEY}
prospectos.ai.groq.base-url=https://api.groq.com/openai
prospectos.ai.groq.model=llama3-8b-8192
```

### OpenAI Configuration
```properties
# OpenAI API Configuration
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.openai.chat.options.model=gpt-4-turbo-preview
```

### Anthropic Configuration
```properties
# Anthropic API Configuration
spring.ai.anthropic.api-key=${ANTHROPIC_API_KEY}
spring.ai.anthropic.chat.options.model=claude-3-5-sonnet-20241022
```

## Profile-Based Configuration

### Development Profile
```properties
# Full AI capabilities enabled
prospectos.ai.enabled=true
prospectos.ai.groq.enabled=true
prospectos.discovery.llm.enabled=true
prospectos.leads.allowed-sources=in-memory,scraper,llm-discovery,vector-company
```

### Production Profile
```properties
# Production-ready configuration
prospectos.ai.enabled=true
prospectos.ai.groq.enabled=true
prospectos.leads.allowed-sources=in-memory,vector-company
prospectos.vectorization.backend=pgvector
```

### Test Profile
```properties
# AI disabled for deterministic testing
prospectos.ai.enabled=false
spring.ai.vectorstore.type=none
prospectos.leads.allowed-sources=in-memory,vector-company
prospectos.vectorization.backend=in-memory
```

### Mock Profile
```properties
# Local development without real AI dependencies
prospectos.ai.enabled=false
prospectos.leads.allowed-sources=in-memory,vector-company
```

## Bean Configuration

### Conditional Bean Creation
All AI-related beans use conditional annotations to ensure proper environment activation:

```java
@Bean("groqChatModel")
@ConditionalOnProperty(
    name = "prospectos.ai.groq.enabled",
    havingValue = "true",
    matchIfMissing = false
)
@Profile("!test")
public ChatModel groqChatModel() {
    // Configuration logic
}
```

### Error Handling
Each configuration includes comprehensive error handling:
- API key validation
- Connection error management
- Graceful fallback mechanisms
- Detailed logging for troubleshooting

## Usage Examples

### Basic Company Scoring
```java
@Autowired
private ScoringAIService scoringService;

public void scoreCompany(Company company, ICP icp) {
    if (aiEnabled) {
        ScoringResult result = scoringService.scoreCompany(company, icp);
        company.updateScore(result.score());
        log.info("Company {} scored: {} ({})", 
            company.getName(), result.score(), result.priority());
    }
}
```

### Outreach Message Generation
```java
@Autowired
private OutreachAIService outreachService;

public void generatePersonalizedOutreach(Company company, ICP icp) {
    OutreachMessage message = outreachService.generateOutreach(company, icp);
    
    // Use the generated message
    sendEmail(company.getPrimaryContact(), message.subject(), message.body());
}
```

### Strategy Recommendations
```java
@Autowired
private StrategyAIService strategyService;

public void recommendOutreachStrategy(Company company, ICP icp) {
    StrategyRecommendation strategy = strategyService.recommendStrategy(company, icp);
    
    company.setRecommendedApproach(strategy.summary());
    company.setPreferredChannel(strategy.channel());
}
```

## System Prompts

### Default System Prompt
The configuration includes a comprehensive system prompt for B2B prospecting:

```
You are a B2B prospecting and company analysis expert.

Your responsibilities:
1. Analyze if companies fit the ICP (Ideal Customer Profile)
2. Calculate fit scores (0-100) based on concrete data
3. Recommend personalized outreach strategies
4. Generate highly personalized outreach messages
5. Identify buying interest signals

Principles:
- Base all decisions on DATA, not assumptions
- Be objective and direct
- Use available functions when you need more information
- Provide clear reasoning for your conclusions
- Scores must be justified with specific criteria

Output format:
- Always return structured JSON when requested
- Be concise but complete
- Include confidence levels in recommendations
```

### Scoring System Prompt
Specialized prompt for scoring services:

```
You are a B2B prospecting scoring system.

Calculate scores (0-100) based on:
1. ICP fit (30 points)
2. Interest signals (25 points)
3. Company size and maturity (20 points)
4. Timing and urgency (15 points)
5. Contact accessibility (10 points)

ALWAYS return JSON with:
- score (0-100)
- reasoning (detailed justification)
- breakdown (points per category)
- priority (HOT/WARM/COLD/IGNORE)
```

## Integration Points

### Spring Modulith Integration
The AI configuration respects Spring Modulith boundaries:
- Core module remains AI-agnostic
- AI services in dedicated `ai` module
- Clean interfaces for cross-module communication

### Vector Store Integration
AI services integrate with the vector store for semantic search:
```properties
prospectos.vectorization.backend=in-memory|pgvector
prospectos.vectorization.model-id=hashing-v1
prospectos.vectorization.embedding-dimension=256
```

### Function Calling
LLMs can call Java functions for enhanced capabilities:
- `scrapeWebsite()`: Web scraping integration
- `searchNews()`: News and signal search
- `analyzeSignals()`: Pattern analysis

## Monitoring and Observability

### Logging Configuration
```properties
# AI-specific logging
logging.level.dev.prospectos.ai=INFO
logging.level.org.springframework.ai=DEBUG
```

### Health Checks
Monitor AI provider availability:
```bash
# Check if AI services are responding
curl -X GET http://localhost:8080/actuator/health
```

### Metrics
Track AI usage and performance:
- Request count per provider
- Response times
- Error rates
- Token usage (if supported by provider)

## Security Considerations

### API Key Management
- Store API keys in environment variables
- Never commit API keys to version control
- Use different keys for different environments
- Implement key rotation policies

### Request Validation
- Validate all inputs before sending to AI providers
- Implement rate limiting
- Monitor for suspicious usage patterns
- Log security-relevant events

## Troubleshooting

### Common Issues

#### AI Services Not Starting
```
Error: ConditionalOnProperty condition not met
```
**Solution**: Check that `prospectos.ai.enabled=true` and provider-specific properties are set.

#### Groq API Connection Failed
```
Error: Unable to initialize Groq ChatModel
```
**Solution**: Verify API key and base URL configuration. Check network connectivity.

#### Test Profile Issues
```
Error: No qualifying bean of type 'ChatModel'
```
**Solution**: Ensure test profile disables AI services with `prospectos.ai.enabled=false`.

### Debug Configuration
Enable debug logging for AI configuration:
```properties
logging.level.dev.prospectos.ai.config=DEBUG
logging.level.org.springframework.boot.autoconfigure=DEBUG
```

### Configuration Validation
Verify your configuration with:
```bash
# Check which beans are registered
./gradlew bootRun --debug | grep -i "ai\|groq\|openai"

# Validate properties
./gradlew bootRun --spring.config.additional-location=file:./debug.properties
```

## Migration Guide

### From Manual Configuration
If migrating from manual AI configuration:

1. Remove hardcoded API clients
2. Add profile-specific properties
3. Update service dependencies to use `AIProvider`
4. Add conditional annotations to existing beans

### Environment Variables
Update your environment configuration:
```bash
# Before
OPENAI_API_KEY=sk-...

# After
GROQ_API_KEY=gsk_...
PROSPECTOS_AI_ENABLED=true
PROSPECTOS_AI_GROQ_ENABLED=true
```

## Future Enhancements

### Planned Features
- Dynamic provider switching based on workload
- A/B testing for different prompts
- Cost optimization across providers
- Enhanced function calling capabilities
- Real-time provider health monitoring

### Configuration Improvements
- Type-safe configuration with `@ConfigurationProperties`
- Centralized prompt management
- Provider-specific retry policies
- Advanced caching strategies