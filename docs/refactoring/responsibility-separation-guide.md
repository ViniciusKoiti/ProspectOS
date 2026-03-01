# Guia de Separação de Responsabilidades - AI Configuration

## Princípios Fundamentais

### Single Responsibility Principle (SRP)
Cada classe deve ter apenas **uma razão para mudar**.

**Problema Atual**:
```java
// SpringAIConfig faz MUITAS coisas (violação SRP)
@Configuration
public class SpringAIConfig {
    // ❌ Cria ChatClients
    // ❌ Define system prompts  
    // ❌ Configura providers
    // ❌ Gerencia dependencies
    // ❌ Define defaults
}
```

**Solução**:
```java
// Dividir em responsabilidades específicas
AIProviderConfig     -> Apenas criação de providers
AIChatClientConfig   -> Apenas criação de ChatClients  
AIPromptConfig       -> Apenas gerenciamento de prompts
AIPropertiesConfig   -> Apenas validação de properties
```

## Mapeamento de Responsabilidades

### 1. Configuração de Providers
**Responsabilidade**: Criar e configurar ChatModels e EmbeddingModels

```java
@Configuration
@ConditionalOnProperty(name = "prospectos.ai.enabled", havingValue = "true")
public class AIProviderConfig {
    
    private final UrlUtil urlUtil;
    
    // APENAS responsável por criar providers
    @Bean("groqChatModel")
    @ConditionalOnProperty(name = "prospectos.ai.groq.enabled", havingValue = "true")
    @Profile("!test")
    public ChatModel groqChatModel() {
        validateGroqConfiguration();
        return createGroqChatModel();
    }
    
    @Bean("groqEmbeddingModel") 
    @ConditionalOnProperty(name = "prospectos.ai.groq.enabled", havingValue = "true")
    @Profile("!test")
    public EmbeddingModel groqEmbeddingModel() {
        validateGroqConfiguration();
        return createGroqEmbeddingModel();
    }
    
    // Métodos privados para validação e criação
    private void validateGroqConfiguration() { /* validação específica Groq */ }
    private ChatModel createGroqChatModel() { /* criação específica */ }
    private EmbeddingModel createGroqEmbeddingModel() { /* criação específica */ }
}
```

### 2. Configuração de ChatClients
**Responsabilidade**: Criar ChatClients com prompts apropriados

```java
@Configuration
@ConditionalOnBean(ChatModel.class)
public class AIChatClientConfig {
    
    private final AIPromptService promptService;
    
    // APENAS responsável por criar ChatClients
    @Bean
    @Primary
    public ChatClient defaultChatClient(ChatModel chatModel) {
        String systemPrompt = promptService.getB2BProspectingPrompt();
        return ChatClient.builder(chatModel)
            .defaultSystem(systemPrompt)
            .build();
    }
    
    @Bean("scoringChatClient")
    @ConditionalOnBean(name = "groqChatModel") 
    public ChatClient scoringChatClient(@Qualifier("groqChatModel") ChatModel chatModel) {
        String systemPrompt = promptService.getScoringPrompt();
        return ChatClient.builder(chatModel)
            .defaultSystem(systemPrompt)
            .build();
    }
}
```

### 3. Gerenciamento de Prompts
**Responsabilidade**: Carregar, versionar e gerenciar system prompts

```java
@Service
public class AIPromptService {
    
    private final Map<PromptType, String> promptCache = new EnumMap<>(PromptType.class);
    
    @PostConstruct
    private void loadPrompts() {
        // APENAS responsável por gerenciar prompts
        promptCache.put(PromptType.B2B_PROSPECTING, loadPromptFromFile("b2b-prospecting.txt"));
        promptCache.put(PromptType.SCORING, loadPromptFromFile("scoring-system.txt"));
        promptCache.put(PromptType.OUTREACH, loadPromptFromFile("outreach-generation.txt"));
        promptCache.put(PromptType.STRATEGY, loadPromptFromFile("strategy-recommendation.txt"));
    }
    
    public String getB2BProspectingPrompt() {
        return getPrompt(PromptType.B2B_PROSPECTING);
    }
    
    public String getScoringPrompt() {
        return getPrompt(PromptType.SCORING);
    }
    
    private String getPrompt(PromptType type) {
        String prompt = promptCache.get(type);
        if (prompt == null) {
            throw new AIConfigurationException("prompt", type.name(), "Prompt não encontrado", null);
        }
        return prompt;
    }
    
    private String loadPromptFromFile(String filename) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("prompts/" + filename)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AIConfigurationException("prompt", filename, "Falha ao carregar prompt", e);
        }
    }
}

enum PromptType {
    B2B_PROSPECTING,
    SCORING, 
    OUTREACH,
    STRATEGY
}
```

### 4. Validação de Properties
**Responsabilidade**: Validar e normalizar configurações

```java
@Component
@ConfigurationProperties(prefix = "prospectos.ai")
@Validated
public class AIProperties {
    
    @NotNull
    private Boolean enabled = true;
    
    @Valid
    private GroqProperties groq = new GroqProperties();
    
    @Valid 
    private OpenAIProperties openai = new OpenAIProperties();
    
    @Data
    @Validated
    public static class GroqProperties {
        
        @NotNull
        private Boolean enabled = false;
        
        @NotBlank(groups = EnabledValidation.class)
        @Pattern(regexp = "^gsk_.*", message = "Groq API key deve começar com 'gsk_'")
        private String apiKey;
        
        @URL(message = "URL base deve ser válida")
        private String baseUrl = "https://api.groq.com/openai";
        
        @NotBlank
        private String model = "llama3-8b-8192";
    }
    
    public interface EnabledValidation {}
}
```

### 5. Utilitários Compartilhados
**Responsabilidade**: Funções utilitárias reutilizáveis

```java
@Component
public class AIConfigUtil {
    
    private static final Logger log = LoggerFactory.getLogger(AIConfigUtil.class);
    
    // APENAS responsável por utilitários de configuração
    public String normalizeGroqUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            return AIConfigProperties.DEFAULT_GROQ_URL;
        }
        
        String normalized = baseUrl.trim();
        
        // Remove trailing slashes
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        
        // Garante endpoint v1
        if (!normalized.endsWith("/v1")) {
            normalized = normalized + "/v1";
        }
        
        log.debug("URL normalizada: {} -> {}", baseUrl, normalized);
        return normalized;
    }
    
    public void validateApiKey(String provider, String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new AIConfigurationException(provider, "api-key", "API key é obrigatória", null);
        }
        
        switch (provider.toLowerCase()) {
            case "groq" -> {
                if (!apiKey.startsWith("gsk_")) {
                    throw new AIConfigurationException(provider, "api-key", "API key Groq deve começar com 'gsk_'", null);
                }
            }
            case "openai" -> {
                if (!apiKey.startsWith("sk-")) {
                    throw new AIConfigurationException(provider, "api-key", "API key OpenAI deve começar com 'sk-'", null);
                }
            }
        }
    }
}
```

## Estrutura de Arquivos Resultante

```
src/main/java/dev/prospectos/ai/
├── config/
│   ├── AIProviderConfig.java          # Criação de providers
│   ├── AIChatClientConfig.java        # Criação de ChatClients
│   ├── AIProperties.java              # Properties com validação
│   └── AIConfigUtil.java              # Utilitários compartilhados
├── service/
│   ├── AIPromptService.java           # Gerenciamento de prompts
│   ├── OutreachAIService.java         # Lógica de outreach
│   ├── ProspectorAIService.java       # Lógica de prospecting
│   ├── ScoringAIService.java          # Lógica de scoring
│   └── StrategyAIService.java         # Lógica de estratégia
└── exception/
    └── AIConfigurationException.java  # Exceções específicas

src/main/resources/
└── prompts/
    ├── b2b-prospecting.txt
    ├── scoring-system.txt
    ├── outreach-generation.txt
    └── strategy-recommendation.txt
```

## Benefícios da Separação

### 1. Manutenibilidade
- **Antes**: Mudança em prompt exige mexer em configuração gigante
- **Depois**: Mudança em prompt = editar apenas `AIPromptService` ou arquivo `.txt`

### 2. Testabilidade
- **Antes**: Tester configuração exige mock de muitas dependências  
- **Depois**: Cada responsabilidade tem testes específicos e focados

### 3. Legibilidade
- **Antes**: Classe de 200+ linhas misturando concerns
- **Depois**: Classes pequenas com propósito claro

### 4. Extensibilidade
- **Antes**: Adicionar provider = modificar classe gigante
- **Depois**: Adicionar provider = nova factory específica

## Exemplo de Uso Prático

### Cenário: Adicionar Novo Provider (Anthropic)

**Com Separação de Responsabilidades**:

1. **Adicionar Properties**:
```java
// Em AIProperties.java - APENAS adicionar nova seção
@Valid
private AnthropicProperties anthropic = new AnthropicProperties();
```

2. **Criar Provider Config**:
```java
// Novo método em AIProviderConfig.java - APENAS provider
@Bean("anthropicChatModel")
@ConditionalOnProperty(name = "prospectos.ai.anthropic.enabled", havingValue = "true")
public ChatModel anthropicChatModel() {
    return createAnthropicChatModel();
}
```

3. **Usar em ChatClient**:
```java
// Em AIChatClientConfig.java - APENAS especificar qual usar
@Bean("anthropicChatClient")
public ChatClient anthropicChatClient(@Qualifier("anthropicChatModel") ChatModel chatModel) {
    return ChatClient.builder(chatModel).build();
}
```

**Resultado**: Novo provider adicionado com **zero mudanças** nas outras responsabilidades!

## Regras de Implementação

### ✅ Fazer
- Uma classe = uma responsabilidade
- Métodos pequenos (< 20 linhas)
- Nomes descritivos
- Logs estruturados em cada operação
- Validação explícita com mensagens claras
- Testes unitários para cada responsabilidade

### ❌ Não Fazer
- Misturar configuração com lógica de negócio
- Classes com mais de 100 linhas
- Métodos que fazem várias coisas
- Magic strings ou numbers
- Generic exception handling
- Dependências circulares entre configs

Esta separação garante código limpo, manutenível e extensível, eliminando os principais code smells identificados.