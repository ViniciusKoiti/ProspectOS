# AI Configuration Refactoring Plan

## Executive Summary

This document outlines a comprehensive refactoring plan for the AI configuration system to address current code smells, improve maintainability, and establish a robust foundation for future AI provider integrations.

## Current State Analysis

### Code Smells Identified

#### 🔴 Critical Issues
1. **Magic String Proliferation**: Configuration property names scattered across classes
2. **Duplicated Logic**: URL normalization repeated in multiple configs
3. **Generic Exception Handling**: Catching `Exception` and re-throwing as `IllegalStateException`
4. **Inconsistent Property Hierarchies**: Mixed naming patterns for configuration properties

#### 🟡 Warning Issues
1. **God Object Pattern**: `SpringAIConfig` doing too much
2. **Embedded Business Logic**: System prompts hardcoded in configuration
3. **Annotation Overuse**: Multiple conditional annotations per bean
4. **Configuration Drift**: Breaking changes affecting multiple test files

## Refactoring Roadmap

### Phase 1: Foundation Cleanup (Week 1-2)
**Goal**: Address critical code smells and establish foundation patterns

#### 1.1 Create Configuration Constants
```java
// src/main/java/dev/prospectos/ai/config/AIConfigurationProperties.java
public final class AIConfigurationProperties {
    
    // Base properties
    public static final String AI_ENABLED = "prospectos.ai.enabled";
    public static final String AI_PROVIDER = "prospectos.ai.provider";
    
    // Groq properties
    public static final String GROQ_ENABLED = "prospectos.ai.groq.enabled";
    public static final String GROQ_API_KEY = "prospectos.ai.groq.api-key";
    public static final String GROQ_BASE_URL = "prospectos.ai.groq.base-url";
    public static final String GROQ_MODEL = "prospectos.ai.groq.model";
    
    // Default values
    public static final String DEFAULT_GROQ_BASE_URL = "https://api.groq.com/openai/v1";
    public static final String DEFAULT_GROQ_MODEL = "llama3-8b-8192";
    
    private AIConfigurationProperties() {} // Utility class
}
```

#### 1.2 Extract URL Utility Service
```java
// src/main/java/dev/prospectos/ai/util/UrlNormalizationService.java
@Component
public class UrlNormalizationService {
    
    private static final Logger log = LoggerFactory.getLogger(UrlNormalizationService.class);
    
    public String normalizeGroqBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            return AIConfigurationProperties.DEFAULT_GROQ_BASE_URL;
        }
        
        String normalized = baseUrl.trim();
        
        // Remove trailing slashes
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        
        // Ensure v1 endpoint
        if (!normalized.endsWith("/v1")) {
            normalized = normalized + "/v1";
        }
        
        log.debug("Normalized Groq URL from '{}' to '{}'", baseUrl, normalized);
        return normalized;
    }
}
```

#### 1.3 Improve Exception Handling
```java
// src/main/java/dev/prospectos/ai/exception/AIConfigurationException.java
public class AIConfigurationException extends RuntimeException {
    private final String provider;
    private final String configurationKey;
    
    public AIConfigurationException(String provider, String configurationKey, String message, Throwable cause) {
        super(String.format("AI Configuration failed for provider '%s' at key '%s': %s", 
              provider, configurationKey, message), cause);
        this.provider = provider;
        this.configurationKey = configurationKey;
    }
    
    // getters...
}

// Usage in GroqChatModelConfig
try {
    return new OpenAiChatModel(openAiApi, options, null, null, null);
} catch (IllegalArgumentException e) {
    throw new AIConfigurationException("groq", "api-key", "Invalid API key format", e);
} catch (RestClientException e) {
    throw new AIConfigurationException("groq", "base-url", "Connection failed", e);
}
```

### Phase 2: Configuration Architecture (Week 3-4)
**Goal**: Implement type-safe, modular configuration system

#### 2.1 Type-Safe Configuration Properties
```java
// src/main/java/dev/prospectos/ai/config/properties/AIProperties.java
@ConfigurationProperties(prefix = "prospectos.ai")
@Validated
@Data
public class AIProperties {
    
    @NotNull
    private Boolean enabled = true;
    
    @NotBlank
    private String defaultProvider = "groq";
    
    @Valid
    private GroqProperties groq = new GroqProperties();
    
    @Valid
    private OpenAIProperties openai = new OpenAIProperties();
    
    @Valid
    private AnthropicProperties anthropic = new AnthropicProperties();
    
    @Data
    public static class GroqProperties {
        private Boolean enabled = false;
        
        @NotBlank(groups = EnabledValidation.class)
        private String apiKey;
        
        @URL
        private String baseUrl = "https://api.groq.com/openai/v1";
        
        @NotBlank
        private String model = "llama3-8b-8192";
    }
    
    // OpenAI and Anthropic properties...
    
    public interface EnabledValidation {}
}
```

#### 2.2 Configuration Factory Pattern
```java
// src/main/java/dev/prospectos/ai/config/factory/ChatModelFactory.java
public interface ChatModelFactory {
    ChatModel createChatModel();
    boolean supports(String provider);
    String getProviderName();
}

@Component
@ConditionalOnProperty(name = "prospectos.ai.groq.enabled", havingValue = "true")
public class GroqChatModelFactory implements ChatModelFactory {
    
    private final AIProperties aiProperties;
    private final UrlNormalizationService urlService;
    
    @Override
    public ChatModel createChatModel() {
        var groqProps = aiProperties.getGroq();
        validateConfiguration(groqProps);
        
        String normalizedUrl = urlService.normalizeGroqBaseUrl(groqProps.getBaseUrl());
        
        // Factory logic...
    }
    
    @Override
    public boolean supports(String provider) {
        return "groq".equalsIgnoreCase(provider);
    }
    
    private void validateConfiguration(GroqProperties props) {
        if (props.getApiKey() == null || props.getApiKey().trim().isEmpty()) {
            throw new AIConfigurationException("groq", "api-key", "API key is required", null);
        }
    }
}
```

#### 2.3 Provider Registry
```java
// src/main/java/dev/prospectos/ai/config/AIProviderRegistry.java
@Component
public class AIProviderRegistry {
    
    private final List<ChatModelFactory> factories;
    private final AIProperties aiProperties;
    
    public AIProviderRegistry(List<ChatModelFactory> factories, AIProperties aiProperties) {
        this.factories = factories;
        this.aiProperties = aiProperties;
    }
    
    @Bean
    @Primary
    public ChatModel primaryChatModel() {
        String defaultProvider = aiProperties.getDefaultProvider();
        
        return factories.stream()
            .filter(factory -> factory.supports(defaultProvider))
            .findFirst()
            .map(ChatModelFactory::createChatModel)
            .orElseThrow(() -> new AIConfigurationException(
                defaultProvider, "provider", "No factory found for provider", null));
    }
    
    public List<String> getAvailableProviders() {
        return factories.stream()
            .map(ChatModelFactory::getProviderName)
            .collect(Collectors.toList());
    }
}
```

### Phase 3: Service Layer Refactoring (Week 5-6)
**Goal**: Extract business logic and improve service architecture

#### 3.1 Prompt Management System
```java
// src/main/java/dev/prospectos/ai/prompt/PromptTemplate.java
public enum PromptTemplate {
    
    B2B_PROSPECTING("b2b-prospecting"),
    SCORING_SYSTEM("scoring-system"),
    OUTREACH_GENERATION("outreach-generation");
    
    private final String templateId;
    
    PromptTemplate(String templateId) {
        this.templateId = templateId;
    }
}

@Component
public class PromptManager {
    
    private final Map<PromptTemplate, String> templates = new EnumMap<>(PromptTemplate.class);
    
    @PostConstruct
    private void loadTemplates() {
        // Load from classpath resources
        templates.put(PromptTemplate.B2B_PROSPECTING, loadTemplate("prompts/b2b-prospecting.txt"));
        templates.put(PromptTemplate.SCORING_SYSTEM, loadTemplate("prompts/scoring-system.txt"));
        // ...
    }
    
    public String getPrompt(PromptTemplate template, Map<String, Object> variables) {
        String promptTemplate = templates.get(template);
        return substituteVariables(promptTemplate, variables);
    }
    
    private String loadTemplate(String resourcePath) {
        try {
            return new String(getClass().getClassLoader()
                .getResourceAsStream(resourcePath).readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load prompt template: " + resourcePath, e);
        }
    }
}
```

#### 3.2 AI Service Configuration
```java
// src/main/java/dev/prospectos/ai/config/AIServiceConfiguration.java
@Configuration
@ConditionalOnProperty(name = "prospectos.ai.enabled", havingValue = "true")
public class AIServiceConfiguration {
    
    @Bean
    public ChatClient defaultChatClient(ChatModel chatModel, PromptManager promptManager) {
        String systemPrompt = promptManager.getPrompt(PromptTemplate.B2B_PROSPECTING, Map.of());
        
        return ChatClient.builder(chatModel)
            .defaultSystem(systemPrompt)
            .build();
    }
    
    @Bean
    public ChatClient scoringChatClient(ChatModel chatModel, PromptManager promptManager) {
        String systemPrompt = promptManager.getPrompt(PromptTemplate.SCORING_SYSTEM, Map.of());
        
        return ChatClient.builder(chatModel)
            .defaultSystem(systemPrompt)
            .build();
    }
}
```

### Phase 4: Advanced Features (Week 7-8)
**Goal**: Implement production-ready features

#### 4.1 Health Checks
```java
// src/main/java/dev/prospectos/ai/health/AIProviderHealthIndicator.java
@Component
public class AIProviderHealthIndicator implements HealthIndicator {
    
    private final List<ChatModelFactory> factories;
    private final AIProperties aiProperties;
    
    @Override
    public Health health() {
        if (!aiProperties.getEnabled()) {
            return Health.down()
                .withDetail("reason", "AI services disabled")
                .build();
        }
        
        Health.Builder healthBuilder = Health.up();
        
        for (ChatModelFactory factory : factories) {
            String provider = factory.getProviderName();
            try {
                // Simple health check
                ChatModel model = factory.createChatModel();
                model.call("test"); // Simple call to verify connectivity
                
                healthBuilder.withDetail(provider, "UP");
            } catch (Exception e) {
                healthBuilder.withDetail(provider, "DOWN - " + e.getMessage());
                healthBuilder.down();
            }
        }
        
        return healthBuilder.build();
    }
}
```

#### 4.2 Metrics and Observability
```java
// src/main/java/dev/prospectos/ai/metrics/AIMetricsConfiguration.java
@Configuration
@ConditionalOnProperty(name = "prospectos.ai.metrics.enabled", havingValue = "true")
public class AIMetricsConfiguration {
    
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
    
    @EventListener
    public void handleAIRequestEvent(AIRequestEvent event) {
        Counter.builder("ai.requests.total")
            .tag("provider", event.getProvider())
            .tag("service", event.getService())
            .tag("status", event.getStatus())
            .register(meterRegistry)
            .increment();
            
        Timer.builder("ai.request.duration")
            .tag("provider", event.getProvider())
            .register(meterRegistry)
            .record(event.getDuration(), TimeUnit.MILLISECONDS);
    }
}
```

#### 4.3 Caching Strategy
```java
// src/main/java/dev/prospectos/ai/cache/AIResponseCacheConfiguration.java
@Configuration
@EnableCaching
public class AIResponseCacheConfiguration {
    
    @Bean
    public CacheManager aiCacheManager() {
        return CaffeineCacheManager.builder()
            .caffeine(Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .recordStats())
            .build();
    }
    
    @Bean
    public KeyGenerator aiCacheKeyGenerator() {
        return (target, method, params) -> {
            StringBuilder key = new StringBuilder();
            key.append(target.getClass().getSimpleName());
            key.append(".");
            key.append(method.getName());
            
            for (Object param : params) {
                key.append("#");
                key.append(Objects.hashCode(param));
            }
            
            return key.toString();
        };
    }
}
```

## Implementation Strategy

### Development Phases

#### Phase 1: Foundation (Weeks 1-2)
- [ ] Create `AIConfigurationProperties` constants class
- [ ] Extract `UrlNormalizationService` utility
- [ ] Implement specific exception types
- [ ] Update existing configurations to use constants
- [ ] Add comprehensive unit tests

#### Phase 2: Architecture (Weeks 3-4)
- [ ] Implement `@ConfigurationProperties` with validation
- [ ] Create factory pattern for AI providers
- [ ] Implement provider registry
- [ ] Migrate existing configurations to new pattern
- [ ] Update integration tests

#### Phase 3: Services (Weeks 5-6)
- [ ] Extract prompt templates to resource files
- [ ] Implement `PromptManager` with variable substitution
- [ ] Refactor service configurations
- [ ] Add prompt versioning support
- [ ] Create service integration tests

#### Phase 4: Production Features (Weeks 7-8)
- [ ] Implement health checks for AI providers
- [ ] Add metrics and observability
- [ ] Implement intelligent caching
- [ ] Add circuit breaker pattern
- [ ] Performance testing and optimization

### Testing Strategy

#### Unit Testing
```java
// Example unit test for factory pattern
@ExtendWith(MockitoExtension.class)
class GroqChatModelFactoryTest {
    
    @Mock
    private UrlNormalizationService urlService;
    
    @InjectMocks
    private GroqChatModelFactory factory;
    
    @Test
    void shouldCreateChatModelWithValidConfiguration() {
        // Given
        AIProperties.GroqProperties groqProps = createValidGroqProps();
        when(urlService.normalizeGroqBaseUrl(anyString())).thenReturn("normalized-url");
        
        // When
        ChatModel result = factory.createChatModel();
        
        // Then
        assertThat(result).isNotNull();
        verify(urlService).normalizeGroqBaseUrl(groqProps.getBaseUrl());
    }
    
    @Test
    void shouldThrowExceptionWithInvalidApiKey() {
        // Given
        AIProperties.GroqProperties groqProps = createInvalidGroqProps();
        
        // When/Then
        assertThatThrownBy(() -> factory.createChatModel())
            .isInstanceOf(AIConfigurationException.class)
            .hasMessageContaining("api-key");
    }
}
```

#### Integration Testing
```java
@SpringBootTest
@TestPropertySource(properties = {
    "prospectos.ai.enabled=true",
    "prospectos.ai.groq.enabled=true",
    "prospectos.ai.groq.api-key=test-key"
})
class AIConfigurationIntegrationTest {
    
    @Autowired
    private AIProviderRegistry registry;
    
    @Test
    void shouldRegisterAvailableProviders() {
        List<String> providers = registry.getAvailableProviders();
        assertThat(providers).contains("groq");
    }
}
```

### Migration Path

#### Backward Compatibility
1. Keep existing configuration properties as deprecated
2. Add compatibility layer for old property names
3. Log warnings when deprecated properties are used
4. Provide migration guide for teams

#### Rollback Plan
1. Feature flags for new configuration system
2. Ability to switch back to old system via property
3. Comprehensive integration tests for both systems
4. Gradual rollout strategy

### Risk Mitigation

#### Configuration Validation
- Implement comprehensive property validation
- Add startup-time configuration checks  
- Provide clear error messages for misconfiguration
- Document all required properties

#### Testing Coverage
- Unit tests for all configuration classes (>90% coverage)
- Integration tests for complete AI flows
- Load testing with different providers
- Chaos engineering for provider failures

#### Documentation
- Update all configuration documentation
- Provide migration examples
- Create troubleshooting guides
- Record architecture decision records (ADRs)

## Success Metrics

### Code Quality Metrics
- [ ] Cyclomatic complexity < 10 for all configuration classes
- [ ] Code coverage > 90% for configuration module
- [ ] Zero critical SonarQube issues
- [ ] PMD/Checkstyle compliance

### Performance Metrics
- [ ] AI service startup time < 5 seconds
- [ ] Configuration validation < 100ms
- [ ] Provider switching < 200ms
- [ ] Memory usage reduction by 15%

### Maintainability Metrics
- [ ] New provider integration < 2 hours
- [ ] Configuration change deployment < 1 hour
- [ ] Bug fix time reduced by 50%
- [ ] Developer onboarding time reduced by 30%

## Timeline

| Week | Phase | Deliverables | Risk Level |
|------|-------|--------------|------------|
| 1-2  | Foundation | Constants, utilities, exceptions | Low |
| 3-4  | Architecture | Type-safe config, factories | Medium |
| 5-6  | Services | Prompt management, service refactor | Medium |
| 7-8  | Production | Health checks, metrics, caching | High |

## Post-Refactoring Benefits

### Developer Experience
- Type-safe configuration with IDE support
- Clear error messages for misconfigurations
- Simplified new provider integration
- Reduced boilerplate code

### Operations
- Better observability and monitoring
- Health check integration
- Configuration validation at startup
- Improved debugging capabilities

### Architecture
- Clean separation of concerns
- Testable components
- Extensible provider system
- Future-proof design patterns

This refactoring plan transforms the current AI configuration from a maintenance burden into a robust, extensible system that supports the long-term growth of the ProspectOS platform.