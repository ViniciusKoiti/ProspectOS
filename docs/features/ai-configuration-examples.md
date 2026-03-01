# AI Configuration Usage Examples

## Quick Start Examples

### 1. Local Development Setup

#### Basic Groq Setup
```bash
# .env file (gitignored)
GROQ_API_KEY=gsk_your_development_key_here
SPRING_PROFILES_ACTIVE=development
```

```properties
# application-development.properties
prospectos.ai.enabled=true
prospectos.ai.groq.enabled=true
prospectos.discovery.llm.enabled=true

# Lead sources for development
prospectos.leads.allowed-sources=in-memory,scraper,llm-discovery,vector-company

# Vector store (in-memory for fast development)
prospectos.vectorization.backend=in-memory
```

#### Multi-Provider Setup
```bash
# .env file with multiple providers
GROQ_API_KEY=gsk_your_groq_key
OPENAI_API_KEY=sk_your_openai_key  
ANTHROPIC_API_KEY=sk-ant-your_anthropic_key

# Provider preference
PROSPECTOS_AI_DEFAULT_PROVIDER=groq
```

### 2. Production Configuration

#### Single Provider (Groq)
```properties
# application-production.properties
spring.profiles.active=production

# AI Configuration
prospectos.ai.enabled=true
prospectos.ai.groq.enabled=true
prospectos.ai.groq.model=llama3-70b-8192

# Production-ready sources
prospectos.leads.allowed-sources=in-memory,vector-company

# PostgreSQL vector store
prospectos.vectorization.backend=pgvector
prospectos.vectorization.pgvector.enabled=true
```

#### High Availability Setup
```properties
# Multiple providers for failover
prospectos.ai.enabled=true
prospectos.ai.groq.enabled=true
prospectos.ai.openai.enabled=true
prospectos.ai.default-provider=groq
prospectos.ai.fallback.enabled=true
```

### 3. Testing Configuration

#### Unit Test Profile
```properties
# application-test.properties
prospectos.ai.enabled=false
spring.ai.vectorstore.type=none
prospectos.vectorization.backend=in-memory

# Minimal lead sources for testing
prospectos.leads.allowed-sources=in-memory,vector-company

# H2 in-memory database
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop
```

#### Integration Test with Mock AI
```java
@SpringBootTest
@TestPropertySource(properties = {
    "prospectos.ai.enabled=true",
    "prospectos.ai.groq.enabled=true", 
    "prospectos.ai.groq.api-key=mock-key"
})
@MockBean(ChatModel.class)  // Mock the actual AI calls
class AIIntegrationTest {
    
    @Test
    void shouldConfigureAIServices() {
        // Test AI service configuration without real API calls
    }
}
```

## Service Usage Examples

### 1. Company Scoring Service

#### Basic Usage
```java
@Service
public class ProspectingService {
    
    private final ScoringAIService scoringService;
    
    public void processNewCompany(Company company, ICP icp) {
        if (aiEnabled()) {
            // AI-powered scoring
            ScoringResult result = scoringService.scoreCompany(company, icp);
            
            company.updateScore(result.score());
            company.setPriority(result.priority());
            company.setScoringBreakdown(result.breakdown());
            
            log.info("Company {} scored: {} ({})", 
                company.getName(), result.score(), result.priority());
        } else {
            // Fallback to rule-based scoring
            int fallbackScore = calculateRuleBasedScore(company, icp);
            company.updateScore(fallbackScore);
        }
    }
    
    private boolean aiEnabled() {
        return aiProperties.getEnabled();
    }
}
```

#### Batch Scoring
```java
@Component
public class BatchScoringService {
    
    @Async
    @Retryable(value = {AIProviderException.class}, maxAttempts = 3)
    public CompletableFuture<ScoringResult> scoreCompanyAsync(Company company, ICP icp) {
        try {
            ScoringResult result = scoringService.scoreCompany(company, icp);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            log.error("Failed to score company {}: {}", company.getName(), e.getMessage());
            throw new AIProviderException("Scoring failed", e);
        }
    }
    
    public void scoreBatch(List<Company> companies, ICP icp) {
        List<CompletableFuture<ScoringResult>> futures = companies.stream()
            .map(company -> scoreCompanyAsync(company, icp))
            .collect(Collectors.toList());
            
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenRun(() -> log.info("Batch scoring completed for {} companies", companies.size()));
    }
}
```

### 2. Outreach Message Generation

#### Personalized Messages
```java
@RestController
public class OutreachController {
    
    private final OutreachAIService outreachService;
    
    @PostMapping("/api/companies/{id}/outreach")
    public ResponseEntity<OutreachMessage> generateOutreach(
            @PathVariable Long id,
            @RequestParam Long icpId) {
        
        Company company = companyService.findById(id);
        ICP icp = icpService.findById(icpId);
        
        try {
            OutreachMessage message = outreachService.generateOutreach(company, icp);
            
            // Track generation for analytics
            outreachMetrics.incrementGenerated(icp.getInterestTheme());
            
            return ResponseEntity.ok(message);
            
        } catch (AIProviderException e) {
            // Fallback to template-based message
            OutreachMessage fallback = templateService.generateFallback(company, icp);
            return ResponseEntity.ok(fallback);
        }
    }
}
```

#### A/B Testing Messages
```java
@Component
public class OutreachVariationService {
    
    public List<OutreachMessage> generateVariations(Company company, ICP icp, int count) {
        List<OutreachMessage> variations = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            // Add variation instruction to prompt
            String variationPrompt = String.format(
                "Create variation #%d with different tone and approach", i + 1);
                
            OutreachMessage variation = outreachService.generateOutreach(
                company, icp, variationPrompt);
            variations.add(variation);
        }
        
        return variations;
    }
}
```

### 3. Strategy Recommendations

#### Dynamic Strategy Selection
```java
@Service
public class StrategyService {
    
    public void recommendApproach(Company company, ICP icp) {
        StrategyRecommendation strategy = strategyService.recommendStrategy(company, icp);
        
        // Apply recommendations to company
        company.setRecommendedApproach(strategy.summary());
        company.setPreferredChannel(strategy.channel());
        company.setOptimalTiming(strategy.timing());
        
        // Create follow-up tasks based on strategy
        createFollowUpTasks(company, strategy);
        
        log.info("Strategy recommended for {}: {} via {}", 
            company.getName(), strategy.summary(), strategy.channel());
    }
    
    private void createFollowUpTasks(Company company, StrategyRecommendation strategy) {
        switch (strategy.channel()) {
            case "linkedin" -> createLinkedInTask(company, strategy);
            case "email" -> createEmailTask(company, strategy);  
            case "phone" -> createPhoneTask(company, strategy);
            case "event" -> createEventTask(company, strategy);
        }
    }
}
```

## Advanced Configuration Examples

### 1. Custom Provider Factory

#### Implementing Custom Provider
```java
@Component
@ConditionalOnProperty(name = "prospectos.ai.custom.enabled", havingValue = "true")
public class CustomAIProviderFactory implements ChatModelFactory {
    
    @Value("${prospectos.ai.custom.endpoint}")
    private String customEndpoint;
    
    @Value("${prospectos.ai.custom.api-key}")
    private String apiKey;
    
    @Override
    public ChatModel createChatModel() {
        // Custom implementation for proprietary AI service
        CustomApiClient client = new CustomApiClient(customEndpoint, apiKey);
        return new CustomChatModel(client);
    }
    
    @Override
    public boolean supports(String provider) {
        return "custom".equalsIgnoreCase(provider);
    }
    
    @Override
    public String getProviderName() {
        return "custom";
    }
}
```

### 2. Dynamic Configuration Updates

#### Runtime Configuration Changes
```java
@RestController
@RequestMapping("/api/admin/ai-config")
public class AIConfigurationController {
    
    private final AIProviderRegistry registry;
    private final ConfigurableEnvironment environment;
    
    @PostMapping("/provider/switch")
    public ResponseEntity<String> switchProvider(@RequestParam String newProvider) {
        try {
            // Validate provider is available
            List<String> available = registry.getAvailableProviders();
            if (!available.contains(newProvider)) {
                return ResponseEntity.badRequest()
                    .body("Provider not available: " + newProvider);
            }
            
            // Update configuration
            System.setProperty("prospectos.ai.default-provider", newProvider);
            
            // Refresh AI beans (requires restart or refresh scope)
            return ResponseEntity.ok("Provider switched to: " + newProvider);
            
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body("Failed to switch provider: " + e.getMessage());
        }
    }
    
    @GetMapping("/status")
    public Map<String, Object> getAIStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("enabled", aiProperties.getEnabled());
        status.put("availableProviders", registry.getAvailableProviders());
        status.put("activeProvider", getCurrentProvider());
        return status;
    }
}
```

### 3. Load Balancing Across Providers

#### Round-Robin Provider Selection
```java
@Component
public class LoadBalancedAIProvider {
    
    private final List<ChatModel> chatModels;
    private final AtomicInteger counter = new AtomicInteger(0);
    
    public LoadBalancedAIProvider(List<ChatModel> chatModels) {
        this.chatModels = chatModels;
    }
    
    public ChatModel getNextProvider() {
        if (chatModels.isEmpty()) {
            throw new IllegalStateException("No AI providers available");
        }
        
        int index = counter.getAndIncrement() % chatModels.size();
        return chatModels.get(index);
    }
    
    @Retryable(value = {AIProviderException.class}, maxAttempts = 3)
    public String callWithFallback(String prompt) {
        ChatModel provider = getNextProvider();
        
        try {
            return provider.call(prompt);
        } catch (Exception e) {
            // Try next provider on failure
            log.warn("Provider failed, trying next: {}", e.getMessage());
            ChatModel fallbackProvider = getNextProvider();
            return fallbackProvider.call(prompt);
        }
    }
}
```

## Docker & Kubernetes Examples

### 1. Docker Compose Setup

#### Development Environment
```yaml
# docker-compose.dev.yml
version: '3.8'
services:
  prospectos-app:
    image: prospectos:latest
    environment:
      - SPRING_PROFILES_ACTIVE=development
      - GROQ_API_KEY=${GROQ_API_KEY}
      - PROSPECTOS_AI_ENABLED=true
      - PROSPECTOS_AI_GROQ_ENABLED=true
    ports:
      - "8080:8080"
    depends_on:
      - postgres
      
  postgres:
    image: pgvector/pgvector:pg16
    environment:
      - POSTGRES_DB=prospectos_dev
      - POSTGRES_USER=prospectos
      - POSTGRES_PASSWORD=devpassword
    ports:
      - "5432:5432"
```

#### Production Environment  
```yaml
# docker-compose.prod.yml
version: '3.8'
services:
  prospectos-app:
    image: prospectos:latest
    environment:
      - SPRING_PROFILES_ACTIVE=production
      - GROQ_API_KEY_FILE=/run/secrets/groq_api_key
      - PROSPECTOS_AI_ENABLED=true
      - PROSPECTOS_VECTORIZATION_BACKEND=pgvector
    secrets:
      - groq_api_key
    deploy:
      replicas: 3
      
secrets:
  groq_api_key:
    external: true
```

### 2. Kubernetes Configuration

#### ConfigMap for AI Settings
```yaml
# ai-configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: prospectos-ai-config
data:
  application.properties: |
    spring.profiles.active=production
    prospectos.ai.enabled=true
    prospectos.ai.groq.enabled=true
    prospectos.vectorization.backend=pgvector
    prospectos.leads.allowed-sources=in-memory,vector-company
```

#### Secret for API Keys
```yaml
# ai-secrets.yaml  
apiVersion: v1
kind: Secret
metadata:
  name: prospectos-ai-secrets
type: Opaque
data:
  groq-api-key: Z3NrX3lvdXJfYmFzZTY0X2VuY29kZWRfa2V5  # base64 encoded
  openai-api-key: c2tfeW91cl9iYXNlNjRfZW5jb2RlZF9rZXk=
```

#### Deployment with AI Configuration
```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: prospectos-app
spec:
  replicas: 3
  selector:
    matchLabels:
      app: prospectos
  template:
    metadata:
      labels:
        app: prospectos
    spec:
      containers:
      - name: prospectos
        image: prospectos:latest
        env:
        - name: GROQ_API_KEY
          valueFrom:
            secretKeyRef:
              name: prospectos-ai-secrets
              key: groq-api-key
        - name: OPENAI_API_KEY
          valueFrom:
            secretKeyRef:
              name: prospectos-ai-secrets  
              key: openai-api-key
        volumeMounts:
        - name: config-volume
          mountPath: /app/config
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 5
      volumes:
      - name: config-volume
        configMap:
          name: prospectos-ai-config
```

## Monitoring & Observability Examples

### 1. Health Check Configuration
```java
@Component
public class AIHealthIndicator implements HealthIndicator {
    
    private final List<ChatModelFactory> factories;
    
    @Override
    public Health health() {
        Health.Builder healthBuilder = Health.up();
        
        for (ChatModelFactory factory : factories) {
            String provider = factory.getProviderName();
            
            try {
                // Quick health check call
                ChatModel model = factory.createChatModel();
                String response = model.call("ping");
                
                healthBuilder.withDetail(provider, Map.of(
                    "status", "UP",
                    "responseTime", measureResponseTime(model),
                    "lastChecked", Instant.now()
                ));
                
            } catch (Exception e) {
                healthBuilder.withDetail(provider, Map.of(
                    "status", "DOWN",
                    "error", e.getMessage(),
                    "lastChecked", Instant.now()
                ));
                healthBuilder.down();
            }
        }
        
        return healthBuilder.build();
    }
}
```

### 2. Metrics Collection
```java
@Component
public class AIMetricsCollector {
    
    private final MeterRegistry meterRegistry;
    private final Timer.Sample sample;
    
    @EventListener
    public void handleAIRequest(AIRequestEvent event) {
        // Request count by provider
        Counter.builder("ai.requests.total")
            .tag("provider", event.getProvider())
            .tag("service", event.getService())
            .tag("status", event.isSuccess() ? "success" : "error")
            .register(meterRegistry)
            .increment();
            
        // Response time
        Timer.builder("ai.request.duration")
            .tag("provider", event.getProvider())
            .register(meterRegistry)
            .record(event.getDuration(), TimeUnit.MILLISECONDS);
            
        // Token usage (if available)
        if (event.getTokensUsed() > 0) {
            Gauge.builder("ai.tokens.used")
                .tag("provider", event.getProvider())
                .register(meterRegistry, event, AIRequestEvent::getTokensUsed);
        }
    }
}
```

### 3. Alerting Configuration
```yaml
# prometheus-alerts.yml
groups:
- name: prospectos-ai
  rules:
  - alert: AIProviderDown
    expr: up{job="prospectos-ai"} == 0
    for: 2m
    labels:
      severity: critical
    annotations:
      summary: "AI provider is down"
      description: "AI provider {{ $labels.provider }} has been down for more than 2 minutes"
      
  - alert: AIResponseTimeSlow  
    expr: histogram_quantile(0.95, ai_request_duration_seconds) > 10
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "AI response time is slow"
      description: "95th percentile response time is {{ $value }}s"
      
  - alert: AIErrorRateHigh
    expr: rate(ai_requests_total{status="error"}[5m]) / rate(ai_requests_total[5m]) > 0.1
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "AI error rate is high"
      description: "Error rate is {{ $value | humanizePercentage }}"
```

These examples provide comprehensive coverage of real-world usage patterns for the AI configuration system, from simple development setups to complex production deployments with monitoring and high availability.