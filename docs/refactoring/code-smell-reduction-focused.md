# Code Smell Reduction - Focused Plan

## Objetivo Principal
Reduzir code smells críticos através da separação clara de responsabilidades e melhor cobertura de informações, mantendo simplicidade na implementação.

## Problemas Críticos Identificados

### 1. Magic Strings (Crítico)
**Problema**: Strings hardcoded espalhadas pelo código
**Solução Simples**:
```java
// Criar uma classe de constantes
public final class AIConfigProperties {
    public static final String AI_ENABLED = "prospectos.ai.enabled";
    public static final String GROQ_ENABLED = "prospectos.ai.groq.enabled";
    public static final String GROQ_API_KEY = "prospectos.ai.groq.api-key";
    public static final String DEFAULT_GROQ_URL = "https://api.groq.com/openai/v1";
}
```

### 2. Lógica Duplicada (Crítico)
**Problema**: URL normalization repetida em `GroqChatModelConfig` e `GroqEmbeddingConfig`
**Solução**:
```java
@Component
public class UrlUtil {
    public String normalizeGroqUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            return AIConfigProperties.DEFAULT_GROQ_URL;
        }
        // Lógica centralizada
        return baseUrl.trim().replaceAll("/$", "") + "/v1";
    }
}
```

### 3. Exception Handling Genérico (Crítico)
**Problema**: `catch (Exception e)` muito genérico
**Solução**:
```java
// Em GroqChatModelConfig
try {
    return new OpenAiChatModel(openAiApi, options, null, null, null);
} catch (IllegalArgumentException e) {
    log.error("Groq API key inválida: {}", e.getMessage());
    throw new AIConfigurationException("groq", "api-key", e.getMessage(), e);
} catch (RestClientException e) {
    log.error("Falha na conexão Groq: {}", e.getMessage());
    throw new AIConfigurationException("groq", "connection", e.getMessage(), e);
}
```

## Separação de Responsabilidades

### Configuração Atual (Problemática)
```
SpringAIConfig (God Object)
├── ChatClient creation
├── System prompts
├── Provider selection  
├── Bean configuration
└── Default settings
```

### Configuração Proposta (Separada)
```
AIConfiguration/
├── AIProviderConfig.java     -> Provider beans
├── AIChatClientConfig.java   -> ChatClient setup
├── AIPromptConfig.java       -> System prompts
└── AIPropertiesConfig.java   -> Properties validation
```

### Implementação da Separação

#### 1. AIProviderConfig.java
```java
@Configuration
@ConditionalOnProperty(name = AIConfigProperties.AI_ENABLED, havingValue = "true")
public class AIProviderConfig {
    
    @Bean("groqChatModel")
    @ConditionalOnProperty(name = AIConfigProperties.GROQ_ENABLED, havingValue = "true")
    public ChatModel groqChatModel(UrlUtil urlUtil) {
        // Apenas criação do provider Groq
    }
    
    @Bean("openaiChatModel") 
    @ConditionalOnProperty(name = "prospectos.ai.openai.enabled", havingValue = "true")
    public ChatModel openaiChatModel() {
        // Apenas criação do provider OpenAI
    }
}
```

#### 2. AIChatClientConfig.java
```java
@Configuration
@ConditionalOnProperty(name = AIConfigProperties.AI_ENABLED, havingValue = "true")
public class AIChatClientConfig {
    
    @Bean
    @Primary
    public ChatClient defaultChatClient(ChatModel chatModel, AIPromptService promptService) {
        return ChatClient.builder(chatModel)
            .defaultSystem(promptService.getB2BProspectingPrompt())
            .build();
    }
    
    @Bean("scoringChatClient")
    public ChatClient scoringChatClient(ChatModel chatModel, AIPromptService promptService) {
        return ChatClient.builder(chatModel)
            .defaultSystem(promptService.getScoringPrompt())
            .build();
    }
}
```

#### 3. AIPromptService.java
```java
@Service
public class AIPromptService {
    
    public String getB2BProspectingPrompt() {
        return loadPromptFromFile("b2b-prospecting-prompt.txt");
    }
    
    public String getScoringPrompt() {
        return loadPromptFromFile("scoring-prompt.txt");
    }
    
    private String loadPromptFromFile(String filename) {
        // Carrega prompts de arquivos de resource
    }
}
```

## Cobertura de Informações

### Tratamento de Erros Melhorado
```java
public class AIConfigurationException extends RuntimeException {
    private final String provider;
    private final String configKey;
    private final String userMessage;
    
    public AIConfigurationException(String provider, String configKey, String message, Throwable cause) {
        super(formatMessage(provider, configKey, message), cause);
        this.provider = provider;
        this.configKey = configKey;
        this.userMessage = generateUserFriendlyMessage(provider, configKey, message);
    }
    
    private static String formatMessage(String provider, String configKey, String message) {
        return String.format("[AI Config Error] Provider: %s, Key: %s, Issue: %s", 
                           provider, configKey, message);
    }
    
    private String generateUserFriendlyMessage(String provider, String configKey, String message) {
        return switch (configKey) {
            case "api-key" -> "Verifique se a API key do " + provider + " está configurada corretamente";
            case "connection" -> "Não foi possível conectar ao " + provider + ". Verifique sua conexão";
            case "base-url" -> "URL base do " + provider + " está incorreta";
            default -> "Erro de configuração no " + provider + ": " + message;
        };
    }
}
```

### Logging Estruturado
```java
// Em cada configuração
@Slf4j
public class GroqChatModelConfig {
    
    @Bean
    public ChatModel groqChatModel(UrlUtil urlUtil) {
        log.info("=== Iniciando configuração Groq ChatModel ===");
        
        // Validação com log detalhado
        if (groqApiKey == null || groqApiKey.trim().isEmpty()) {
            log.error("GROQ_API_KEY não configurada");
            throw new AIConfigurationException("groq", "api-key", "API key é obrigatória", null);
        }
        
        String normalizedUrl = urlUtil.normalizeGroqUrl(groqBaseUrl);
        log.info("Groq URL normalizada: {} -> {}", groqBaseUrl, normalizedUrl);
        
        try {
            ChatModel model = createGroqChatModel(normalizedUrl);
            log.info("✅ Groq ChatModel configurado com sucesso");
            return model;
        } catch (Exception e) {
            log.error("❌ Falha ao configurar Groq ChatModel: {}", e.getMessage());
            throw new AIConfigurationException("groq", "creation", e.getMessage(), e);
        }
    }
}
```

### Health Check Informativo
```java
@Component
public class AIHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();
        
        // Verifica cada provider individualmente
        checkProvider("groq", details);
        checkProvider("openai", details);
        
        boolean anyUp = details.values().stream()
            .anyMatch(status -> "UP".equals(status));
            
        return anyUp ? 
            Health.up().withDetails(details).build() :
            Health.down().withDetails(details).build();
    }
    
    private void checkProvider(String provider, Map<String, Object> details) {
        try {
            // Test básico do provider
            details.put(provider, "UP");
            details.put(provider + "_lastCheck", Instant.now());
        } catch (Exception e) {
            details.put(provider, "DOWN");
            details.put(provider + "_error", e.getMessage());
            details.put(provider + "_lastCheck", Instant.now());
        }
    }
}
```

## Plano de Implementação Simplificado

### Fase 1: Constantes e Utilitários (1 semana)
- [ ] Criar `AIConfigProperties` com todas as constantes
- [ ] Criar `UrlUtil` para normalização de URLs
- [ ] Substituir magic strings nas classes existentes
- [ ] Testes unitários básicos

### Fase 2: Separação de Configurações (1 semana)
- [ ] Dividir `SpringAIConfig` em 3 classes menores
- [ ] Extrair prompts para `AIPromptService`
- [ ] Mover system prompts para arquivos `.txt`
- [ ] Testes de integração

### Fase 3: Tratamento de Erros (1 semana)
- [ ] Criar `AIConfigurationException`
- [ ] Substituir generic exception handling
- [ ] Melhorar logging em todas as configurações
- [ ] Adicionar `AIHealthIndicator`

## Validação de Sucesso

### Code Smells Eliminados
- ✅ Zero magic strings no código
- ✅ Zero lógica duplicada
- ✅ Zero generic exception catching
- ✅ Cada classe tem uma responsabilidade clara

### Cobertura de Informações
- ✅ Logs estruturados em todas as operações
- ✅ Mensagens de erro user-friendly
- ✅ Health checks informativos
- ✅ Prompts externalizados e versionáveis

### Métricas
- **Complexidade Ciclomática**: < 10 para todas as classes
- **Cobertura de Testes**: > 85%
- **Linhas por Método**: < 20
- **Duplicação de Código**: 0%

Este plano foca na redução efetiva dos code smells principais mantendo a simplicidade e melhorando significativamente a separação de responsabilidades e cobertura de informações.