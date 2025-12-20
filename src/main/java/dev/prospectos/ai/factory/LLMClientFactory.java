package dev.prospectos.ai.factory;

import dev.prospectos.ai.client.LLMClient;
import dev.prospectos.ai.client.LLMProvider;
import dev.prospectos.ai.client.impl.MockLLMClient;
import dev.prospectos.ai.client.impl.SpringAILLMClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

/**
 * Factory para criação de LLMClients
 * Implementa strategy pattern para diferentes provedores
 */
@Slf4j
@Component
public class LLMClientFactory {
    
    @Value("${spring.ai.openai.api-key:}")
    private String openaiKey;
    
    @Value("${spring.ai.anthropic.api-key:}")
    private String anthropicKey;
    
    private final Optional<ChatClient> chatClient;
    private final Optional<ChatClient> scoringChatClient;
    private final Environment environment;
    
    public LLMClientFactory(Optional<ChatClient> chatClient, 
                           Optional<ChatClient> scoringChatClient,
                           Environment environment) {
        this.chatClient = chatClient;
        this.scoringChatClient = scoringChatClient;
        this.environment = environment;
    }
    
    /**
     * Cria LLMClient principal (para queries gerais)
     */
    public LLMClient createPrimaryClient() {
        return createClient(LLMProvider.OPENAI, chatClient);
    }
    
    /**
     * Cria LLMClient para scoring (especializado)
     */
    public LLMClient createScoringClient() {
        return createClient(LLMProvider.OPENAI, scoringChatClient);
    }
    
    /**
     * Cria LLMClient para um provedor específico
     */
    public LLMClient createClient(LLMProvider provider) {
        return switch (provider) {
            case OPENAI -> createClient(provider, chatClient);
            case ANTHROPIC -> createClient(provider, chatClient); // Future: separate Claude client
            case OLLAMA -> createClient(provider, chatClient);    // Future: separate Ollama client
            case MOCK -> createMockClient();
        };
    }
    
    /**
     * Detecta e cria o melhor cliente disponível
     */
    public LLMClient createBestAvailableClient() {
        if (isOpenAIAvailable()) {
            log.info("🤖 Using OpenAI as primary provider");
            return createClient(LLMProvider.OPENAI, chatClient);
        }
        
        if (isAnthropicAvailable()) {
            log.info("🤖 Using Anthropic as primary provider");
            return createClient(LLMProvider.ANTHROPIC, chatClient);
        }
        
        log.warn("🤖 No LLM provider configured. Using Mock for testing.");
        return createMockClient();
    }
    
    private LLMClient createClient(LLMProvider provider, Optional<ChatClient> chatClientOpt) {
        boolean available = isProviderAvailable(provider);
        
        if (chatClientOpt.isPresent() && available) {
            log.debug("✅ Creating {} client - available", provider.getDisplayName());
            return new SpringAILLMClient(chatClientOpt.get(), provider, true);
        } else {
            log.debug("🧪 Creating {} client - mock (test env or not available)", provider.getDisplayName());
            // Em teste, criar SpringAILLMClient com mock behavior, mas mantém o provider correto
            return new SpringAILLMClient(null, provider, false);
        }
    }
    
    private LLMClient createMockClient() {
        log.debug("🧪 Creating Mock LLM client");
        return new MockLLMClient();
    }
    
    private boolean isProviderAvailable(LLMProvider provider) {
        return switch (provider) {
            case OPENAI -> isOpenAIAvailable();
            case ANTHROPIC -> isAnthropicAvailable();
            case OLLAMA -> isOllamaAvailable();
            case MOCK -> true;
        };
    }
    
    private boolean isOpenAIAvailable() {
        return isValidApiKey(openaiKey);
    }
    
    private boolean isAnthropicAvailable() {
        return isValidApiKey(anthropicKey);
    }
    
    /**
     * Detecta se está rodando em contexto de teste
     */
    private boolean isTestEnvironment() {
        return Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> profile.contains("test"))
                || environment.getProperty("spring.profiles.active", "").contains("test")
                || isRunningInTestContext();
    }
    
    /**
     * Detecta se está rodando em contexto de teste através de stack trace
     */
    private boolean isRunningInTestContext() {
        return Arrays.stream(Thread.currentThread().getStackTrace())
                .anyMatch(element -> element.getClassName().contains("junit")
                        || element.getClassName().contains("Test")
                        || element.getClassName().contains("test"));
    }
    
    /**
     * Valida se a chave de API é válida (não é chave de teste/desenvolvimento)
     */
    private boolean isValidApiKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            return false;
        }
        
        String trimmedKey = key.trim();
        
        // Lista de padrões inválidos
        return !trimmedKey.equals("test-key")
                && !trimmedKey.equals("dummy-key")
                && !trimmedKey.equals("fake-key")
                && !trimmedKey.equals("mock-key")
                && !trimmedKey.startsWith("sk-test-")
                && !trimmedKey.matches("(?i)test.*|mock.*|fake.*|dummy.*|dev.*");
    }
    
    private boolean isOllamaAvailable() {
        return false;
    }
}