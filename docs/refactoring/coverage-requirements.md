# Requisitos de Cobertura - AI Configuration Refactoring

## Cobertura de Código

### Metas de Cobertura
| Componente | Cobertura Mínima | Cobertura Alvo | Prioridade |
|------------|------------------|----------------|------------|
| **Configuration Classes** | 85% | 95% | Alta |
| **Service Layer** | 80% | 90% | Alta |
| **Utility Classes** | 90% | 95% | Média |
| **Exception Classes** | 70% | 85% | Baixa |

### Cobertura por Tipo de Teste

#### Unit Tests (Cobertura Individual)
```java
// Exemplo: AIProviderConfigTest.java
@ExtendWith(MockitoExtension.class)
class AIProviderConfigTest {
    
    @Mock private AIConfigUtil configUtil;
    @InjectMocks private AIProviderConfig config;
    
    @Test
    void shouldCreateGroqChatModelWhenConfigurationValid() {
        // Testa APENAS criação do provider
        // Cobertura: método createGroqChatModel()
    }
    
    @Test
    void shouldThrowExceptionWhenApiKeyInvalid() {
        // Testa APENAS validação
        // Cobertura: método validateGroqConfiguration()
    }
    
    @Test
    void shouldSkipCreationWhenPropertyDisabled() {
        // Testa APENAS condicional
        // Cobertura: @ConditionalOnProperty behavior
    }
}
```

#### Integration Tests (Cobertura End-to-End)
```java
@SpringBootTest
@TestPropertySource(properties = {
    "prospectos.ai.enabled=true",
    "prospectos.ai.groq.enabled=true", 
    "prospectos.ai.groq.api-key=test-key"
})
class AIConfigurationIntegrationTest {
    
    @Test
    void shouldConfigureCompleteAIStack() {
        // Testa integração completa entre todas as responsabilidades
        // Cobertura: fluxo completo de configuração
    }
}
```

## Cobertura de Informações

### 1. Logging Estruturado

#### Níveis de Log Obrigatórios
```java
public class AIProviderConfig {
    
    @Bean
    public ChatModel groqChatModel() {
        log.info("=== Iniciando configuração Groq ChatModel ===");           // INFO: Início
        log.debug("Validando configuração Groq");                            // DEBUG: Processo
        
        try {
            log.info("✅ Groq ChatModel configurado com sucesso");           // INFO: Sucesso
            return model;
        } catch (AIConfigurationException e) {
            log.error("❌ Falha na configuração Groq: {}", e.getUserMessage()); // ERROR: Falha
            throw e;
        }
    }
}
```

#### Estrutura de Log Padronizada
```java
// Template obrigatório para logs de configuração
log.info("[AI Config] Component: {}, Provider: {}, Status: {}, Details: {}", 
         component, provider, status, details);

// Exemplos:
log.info("[AI Config] Component: ChatModel, Provider: groq, Status: starting, Details: validating api-key");
log.info("[AI Config] Component: ChatModel, Provider: groq, Status: success, Details: model created");  
log.error("[AI Config] Component: ChatModel, Provider: groq, Status: failed, Details: invalid api-key");
```

### 2. Exception Handling com Contexto

#### Informações Obrigatórias em Exceções
```java
public class AIConfigurationException extends RuntimeException {
    
    // OBRIGATÓRIO: Contexto completo
    private final String provider;        // "groq", "openai", etc.
    private final String configKey;       // "api-key", "base-url", etc. 
    private final String technicalMsg;    // Para logs/debug
    private final String userMsg;         // Para usuários finais
    private final Instant timestamp;      // Quando aconteceu
    
    public AIConfigurationException(String provider, String configKey, String message, Throwable cause) {
        super(formatTechnicalMessage(provider, configKey, message), cause);
        this.provider = provider;
        this.configKey = configKey;
        this.technicalMsg = message;
        this.userMsg = generateUserMessage(provider, configKey, message);
        this.timestamp = Instant.now();
    }
    
    // OBRIGATÓRIO: Mensagens user-friendly
    private String generateUserMessage(String provider, String configKey, String message) {
        return switch (configKey) {
            case "api-key" -> String.format("Configure a API key do %s corretamente", provider);
            case "base-url" -> String.format("Verifique a URL de configuração do %s", provider);
            case "connection" -> String.format("Não foi possível conectar ao %s", provider);
            default -> String.format("Erro de configuração no %s: %s", provider, message);
        };
    }
}
```

### 3. Health Check Informativo

#### Informações Obrigatórias no Health Check
```java
@Component
public class AIHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();
        
        // OBRIGATÓRIO: Status de cada provider
        details.put("providers", checkAllProviders());
        
        // OBRIGATÓRIO: Configurações ativas  
        details.put("configuration", getActiveConfiguration());
        
        // OBRIGATÓRIO: Métricas básicas
        details.put("metrics", getBasicMetrics());
        
        // OBRIGATÓRIO: Último teste de conectividade
        details.put("lastHealthCheck", Instant.now());
        
        return Health.up().withDetails(details).build();
    }
    
    private Map<String, Object> checkAllProviders() {
        Map<String, Object> providerStatus = new HashMap<>();
        
        // Informações detalhadas OBRIGATÓRIAS para cada provider
        providerStatus.put("groq", Map.of(
            "status", isGroqHealthy() ? "UP" : "DOWN",
            "configured", isGroqConfigured(),
            "lastSuccessfulCall", getLastSuccessfulCall("groq"),
            "errorCount", getErrorCount("groq"),
            "responseTimeMs", getAverageResponseTime("groq")
        ));
        
        return providerStatus;
    }
}
```

### 4. Configuration Properties Documentation

#### Auto-documentação Obrigatória
```java
@ConfigurationProperties(prefix = "prospectos.ai")
@Validated
@Data
public class AIProperties {
    
    /**
     * Habilita ou desabilita todos os serviços de AI.
     * 
     * @default true
     * @example true
     * @since 1.0
     */
    @NotNull
    private Boolean enabled = true;
    
    /**
     * Configurações específicas do provider Groq.
     */
    @Valid
    private GroqProperties groq = new GroqProperties();
    
    @Data
    @Validated  
    public static class GroqProperties {
        
        /**
         * Habilita o provider Groq para chat e embeddings.
         * 
         * @default false  
         * @example true
         * @requires prospectos.ai.enabled=true
         */
        @NotNull
        private Boolean enabled = false;
        
        /**
         * API key do Groq. Obrigatória quando groq.enabled=true.
         * 
         * @format Deve começar com 'gsk_'
         * @example gsk_1234567890abcdef
         * @secret true
         */
        @NotBlank(groups = EnabledValidation.class)
        @Pattern(regexp = "^gsk_.*", message = "API key Groq deve começar com 'gsk_'")
        private String apiKey;
    }
}
```

## Cobertura de Validação

### 1. Startup Validation

#### Validações Obrigatórias no Startup
```java
@Component
public class AIConfigurationValidator {
    
    @EventListener(ApplicationReadyEvent.class)
    public void validateConfiguration() {
        List<String> errors = new ArrayList<>();
        
        // OBRIGATÓRIO: Validar consistência de configuração
        validateProviderConsistency(errors);
        validateRequiredProperties(errors);
        validateProviderConnectivity(errors);
        
        if (!errors.isEmpty()) {
            String errorSummary = String.join("\n", errors);
            log.error("❌ Configuração AI inválida:\n{}", errorSummary);
            throw new IllegalStateException("Configuração AI inválida: " + errorSummary);
        }
        
        log.info("✅ Configuração AI validada com sucesso");
    }
    
    private void validateProviderConsistency(List<String> errors) {
        // Se AI está habilitado, pelo menos 1 provider deve estar habilitado
        if (aiProperties.getEnabled()) {
            boolean anyProviderEnabled = aiProperties.getGroq().getEnabled() 
                                       || aiProperties.getOpenai().getEnabled();
            
            if (!anyProviderEnabled) {
                errors.add("AI habilitado mas nenhum provider configurado");
            }
        }
    }
}
```

### 2. Runtime Validation

#### Validações em Tempo de Execução
```java
@Service
public class AIServiceValidator {
    
    // OBRIGATÓRIO: Validar antes de cada chamada AI
    public void validateBeforeAICall(String provider, String operation) {
        
        // Validar rate limiting
        if (isRateLimited(provider)) {
            throw new AIServiceException(provider, "rate-limit", "Rate limit excedido");
        }
        
        // Validar provider health
        if (!isProviderHealthy(provider)) {
            throw new AIServiceException(provider, "unhealthy", "Provider não está saudável");
        }
        
        // Validar operação suportada
        if (!isOperationSupported(provider, operation)) {
            throw new AIServiceException(provider, "unsupported", "Operação não suportada");
        }
        
        log.debug("[Validation] Provider: {}, Operation: {}, Status: valid", provider, operation);
    }
}
```

## Cobertura de Testes

### 1. Test Coverage Matrix

| Classe | Unit Tests | Integration Tests | Performance Tests | Security Tests |
|--------|------------|-------------------|-------------------|----------------|
| AIProviderConfig | ✅ 95% | ✅ Incluída | ⚠️ Startup time | ⚠️ API key exposure |
| AIChatClientConfig | ✅ 90% | ✅ Incluída | ➖ N/A | ➖ N/A |  
| AIPromptService | ✅ 85% | ✅ Incluída | ➖ N/A | ⚠️ Prompt injection |
| AIProperties | ✅ 95% | ✅ Incluída | ➖ N/A | ✅ Validation |
| AIConfigUtil | ✅ 100% | ➖ N/A | ➖ N/A | ⚠️ URL validation |

### 2. Test Scenarios Obrigatórios

#### Configuration Tests
```java
// OBRIGATÓRIO: Testar todos os cenários de configuração
@ParameterizedTest
@ValueSource(strings = {"", "invalid-key", "sk-openai-key", "gsk-malformed"})
void shouldValidateApiKeyFormats(String apiKey) {
    // Testa validação de formato para cada provider
}

@Test
void shouldFailWhenRequiredPropertiesMissing() {
    // Testa falha quando propriedades obrigatórias estão ausentes
}

@Test  
void shouldCreateBeansInCorrectOrder() {
    // Testa ordem de criação dos beans (providers antes de clients)
}
```

#### Service Tests
```java
// OBRIGATÓRIO: Testar integração entre serviços
@Test
void shouldInjectCorrectChatClientInServices() {
    // Testa se cada serviço recebe o ChatClient correto
}

@Test
void shouldHandleProviderFailureGracefully() {
    // Testa comportamento quando provider falha
}
```

### 3. Test Data Coverage

#### OBRIGATÓRIO: Cenários de Teste
- ✅ Configuração válida completa
- ✅ Configuração com apenas Groq habilitado
- ✅ Configuração com apenas OpenAI habilitado  
- ✅ Configuração sem providers (AI desabilitado)
- ✅ Configuração com API keys inválidas
- ✅ Configuração com URLs malformadas
- ✅ Configuração em diferentes profiles
- ✅ Migração de configuração antiga para nova

## Métricas de Qualidade

### Code Quality Gates
```yaml
# OBRIGATÓRIO: Gates de qualidade
coverage:
  minimum: 85%
  target: 90%
  
complexity:
  maximum: 10
  target: 7
  
duplication:
  maximum: 3%
  target: 0%
  
maintainability:
  minimum: B
  target: A
```

### Monitoring Requirements
```java
// OBRIGATÓRIO: Métricas em produção
@Component
public class AIConfigurationMetrics {
    
    // Contadores obrigatórios
    @Counter("ai.configuration.startup.total")
    public void countStartup() {}
    
    @Counter("ai.configuration.errors.total") 
    public void countConfigurationError(String provider, String error) {}
    
    @Timer("ai.provider.initialization.time")
    public void timeProviderInitialization(String provider) {}
    
    @Gauge("ai.providers.active.count")
    public int activeProviders() { return getActiveProviderCount(); }
}
```

Esta cobertura garante informações completas sobre configuração, erros, performance e qualidade do sistema AI.