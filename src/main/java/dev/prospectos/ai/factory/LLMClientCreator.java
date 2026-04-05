package dev.prospectos.ai.factory;

import dev.prospectos.ai.client.LLMClient;
import dev.prospectos.ai.client.LLMProvider;
import dev.prospectos.ai.client.LlmScoringResponseConverter;
import dev.prospectos.ai.client.LlmStructuredResponseSanitizer;
import dev.prospectos.ai.client.impl.MockLLMClient;
import dev.prospectos.ai.client.impl.SpringAILLMClient;
import dev.prospectos.ai.client.impl.SpringAIToolResolver;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;

final class LLMClientCreator {

    private final ObjectProvider<ChatClient> chatClient;
    private final ObjectProvider<ChatClient> scoringChatClient;
    private final ObjectProvider<ChatClient> groqChatClient;
    private final ObjectProvider<ChatClient> groqScoringChatClient;
    private final LLMProviderAvailabilityChecker availabilityChecker;
    private final SpringAIToolResolver toolResolver;

    LLMClientCreator(
        ObjectProvider<ChatClient> chatClient,
        ObjectProvider<ChatClient> scoringChatClient,
        ObjectProvider<ChatClient> groqChatClient,
        ObjectProvider<ChatClient> groqScoringChatClient,
        LLMProviderAvailabilityChecker availabilityChecker,
        SpringAIToolResolver toolResolver
    ) {
        this.chatClient = chatClient;
        this.scoringChatClient = scoringChatClient;
        this.groqChatClient = groqChatClient;
        this.groqScoringChatClient = groqScoringChatClient;
        this.availabilityChecker = availabilityChecker;
        this.toolResolver = toolResolver;
    }

    LLMClient createProviderClient(LLMProvider provider, boolean scoring) {
        if (provider == LLMProvider.MOCK) {
            return createMockClient();
        }
        ObjectProvider<ChatClient> providerClient = resolveClientProvider(provider, scoring);
        if (providerClient == null) {
            return null;
        }
        boolean available = availabilityChecker.isProviderAvailable(provider);
        ChatClient client = providerClient.getIfAvailable();
        return new SpringAILLMClient(client, provider, client != null && available,
            new LlmScoringResponseConverter(new LlmStructuredResponseSanitizer()),
            toolResolver);
    }

    LLMClient createMockClient() {
        return new MockLLMClient();
    }

    LLMClient createMockClient(LLMProvider provider) {
        return new MockLLMClient(provider);
    }

    private ObjectProvider<ChatClient> resolveClientProvider(LLMProvider provider, boolean scoring) {
        return switch (provider) {
            case GROQ -> scoring ? groqScoringChatClient : groqChatClient;
            case OPENAI, ANTHROPIC, OLLAMA -> scoring ? scoringChatClient : chatClient;
            case MOCK -> null;
        };
    }
}
