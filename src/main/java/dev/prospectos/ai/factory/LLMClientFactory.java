package dev.prospectos.ai.factory;

import dev.prospectos.ai.client.LLMClient;
import dev.prospectos.ai.client.LLMProvider;
import dev.prospectos.ai.client.impl.MockLLMClient;
import dev.prospectos.ai.client.impl.SpringAILLMClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Factory for creating LLMClients.
 * Implements the strategy pattern for different providers.
 */
@Slf4j
@Component
public class LLMClientFactory {
    
    @Value("${spring.ai.openai.api-key:}")
    private String openaiKey;
    
    @Value("${spring.ai.anthropic.api-key:}")
    private String anthropicKey;

    @Value("${prospectos.ai.groq.api-key:}")
    private String groqKey;

    private final ObjectProvider<ChatClient> chatClient;
    private final ObjectProvider<ChatClient> scoringChatClient;
    private final ObjectProvider<ChatClient> groqChatClient;
    private final ObjectProvider<ChatClient> groqScoringChatClient;
    private final Environment environment;
    
    public LLMClientFactory(@Qualifier("chatClient") ObjectProvider<ChatClient> chatClient,
                           @Qualifier("scoringChatClient") ObjectProvider<ChatClient> scoringChatClient,
                           @Qualifier("groqChatClient") ObjectProvider<ChatClient> groqChatClient,
                           @Qualifier("groqScoringChatClient") ObjectProvider<ChatClient> groqScoringChatClient,
                           Environment environment) {
        this.chatClient = chatClient;
        this.scoringChatClient = scoringChatClient;
        this.groqChatClient = groqChatClient;
        this.groqScoringChatClient = groqScoringChatClient;
        this.environment = environment;
    }
    
    /**
     * Creates the primary LLMClient (for general queries).
     */
    public LLMClient createPrimaryClient() {
        if (isTestEnvironment()) {
            return createMockClient();
        }
        return createClient(LLMProvider.OPENAI, chatClient);
    }
    
    /**
     * Creates the scoring LLMClient (specialized).
     */
    public LLMClient createScoringClient() {
        if (isTestEnvironment()) {
            return createMockClient();
        }
        return createClient(LLMProvider.OPENAI, scoringChatClient);
    }
    
    /**
     * Creates an LLMClient for a specific provider.
     */
    public LLMClient createClient(LLMProvider provider) {
        if (isTestEnvironment() && provider != LLMProvider.MOCK) {
            return createMockClient(provider);
        }
        return switch (provider) {
            case OPENAI -> createClient(provider, chatClient);
            case ANTHROPIC -> createClient(provider, chatClient); // Future: separate Claude client
            case GROQ -> createClient(provider, groqChatClient);
            case OLLAMA -> createClient(provider, chatClient);    // Future: separate Ollama client
            case MOCK -> createMockClient();
        };
    }
    
    /**
     * Detects and creates the best available client.
     */
    public LLMClient createBestAvailableClient() {
        if (isTestEnvironment()) {
            log.info("Test profile detected. Using Mock provider.");
            return createMockClient();
        }
        if (isGroqAvailable()) {
            log.info("Using Groq as primary provider");
            return createClient(LLMProvider.GROQ, groqChatClient);
        }
        if (isOpenAIAvailable()) {
            log.info("Using OpenAI as primary provider");
            return createClient(LLMProvider.OPENAI, chatClient);
        }
        
        if (isAnthropicAvailable()) {
            log.info("Using Anthropic as primary provider");
            return createClient(LLMProvider.ANTHROPIC, chatClient);
        }
        
        log.warn("No LLM provider configured. Using Mock for testing.");
        return createMockClient();
    }

    private LLMClient createBestAvailableScoringClient() {
        if (isTestEnvironment()) {
            log.info("Test profile detected. Using Mock provider for scoring.");
            return createMockClient();
        }
        if (isGroqAvailable()) {
            log.info("Using Groq for scoring");
            return createClient(LLMProvider.GROQ, groqScoringChatClient);
        }

        if (isOpenAIAvailable()) {
            log.info("Using OpenAI for scoring");
            return createClient(LLMProvider.OPENAI, scoringChatClient);
        }

        if (isAnthropicAvailable()) {
            log.info("Using Anthropic for scoring");
            return createClient(LLMProvider.ANTHROPIC, scoringChatClient);
        }

        log.warn("No scoring LLM provider configured. Using Mock for testing.");
        return createMockClient();
    }

    private LLMClient createClient(LLMProvider provider, ObjectProvider<ChatClient> chatClientProvider) {
        boolean available = isProviderAvailable(provider);

        ChatClient client = chatClientProvider.getIfAvailable();
        if (client != null && available) {
            log.debug("Creating {} client - available", provider.getDisplayName());
            return new SpringAILLMClient(client, provider, true);
        } else {
            log.debug("Creating {} client - mock (test env or not available)", provider.getDisplayName());
            // In tests, create SpringAILLMClient with mock behavior but keep the correct provider
            return new SpringAILLMClient(null, provider, false);
        }
    }
    
    private LLMClient createMockClient() {
        log.debug("Creating Mock LLM client");
        return new MockLLMClient();
    }

    private LLMClient createMockClient(LLMProvider provider) {
        log.debug("Creating Mock LLM client for {}", provider.getDisplayName());
        return new MockLLMClient(provider);
    }

    private boolean isProviderAvailable(LLMProvider provider) {
        return switch (provider) {
            case OPENAI -> isOpenAIAvailable();
            case ANTHROPIC -> isAnthropicAvailable();
            case GROQ -> isGroqAvailable();
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

    private boolean isGroqAvailable() {
        return isValidApiKey(groqKey);
    }

    /**
     * Detects whether running in a test context.
     */
    private boolean isTestEnvironment() {
        return Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> profile.contains("test"))
                || environment.getProperty("spring.profiles.active", "").contains("test")
                || isRunningInTestContext();
    }
    
    /**
     * Detects whether running in a test context via stack trace.
     */
    private boolean isRunningInTestContext() {
        return Arrays.stream(Thread.currentThread().getStackTrace())
                .anyMatch(element -> element.getClassName().contains("junit")
                        || element.getClassName().contains("Test")
                        || element.getClassName().contains("test"));
    }
    
    /**
     * Validates if the API key is valid (not a test/development key).
     */
    private boolean isValidApiKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            return false;
        }
        
        String trimmedKey = key.trim();
        
        // List of invalid patterns
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
