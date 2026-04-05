package dev.prospectos.ai.client.impl;

import dev.prospectos.ai.client.LLMClient;
import dev.prospectos.ai.client.LLMProvider;
import dev.prospectos.ai.client.LlmScoringResponseConverter;
import dev.prospectos.ai.client.LlmStructuredResponseSanitizer;
import dev.prospectos.ai.client.mock.MockResponseFactory;
import dev.prospectos.ai.dto.ScoringResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;

@Slf4j
public class SpringAILLMClient implements LLMClient {

    private final ChatClient chatClient;
    private final LLMProvider provider;
    private final boolean available;
    private final LlmScoringResponseConverter scoringResponseConverter;
    private final SpringAIToolResolver toolResolver;

    public SpringAILLMClient(ChatClient chatClient, LLMProvider provider, boolean available) {
        this(
            chatClient,
            provider,
            available,
            new LlmScoringResponseConverter(new LlmStructuredResponseSanitizer()),
            SpringAIToolResolver.empty()
        );
    }

    public SpringAILLMClient(ChatClient chatClient, LLMProvider provider, boolean available,
                             LlmScoringResponseConverter scoringResponseConverter) {
        this(chatClient, provider, available, scoringResponseConverter, SpringAIToolResolver.empty());
    }

    public SpringAILLMClient(ChatClient chatClient, LLMProvider provider, boolean available,
                             LlmScoringResponseConverter scoringResponseConverter,
                             SpringAIToolResolver toolResolver) {
        this.chatClient = chatClient;
        this.provider = provider;
        this.available = available;
        this.scoringResponseConverter = scoringResponseConverter;
        this.toolResolver = toolResolver;
    }

    @Override
    public String query(String prompt) {
        if (!available) {
            return "Mock response: " + prompt.substring(0, Math.min(50, prompt.length()));
        }
        try {
            log.debug("Executing query on {}: {}", provider.getDisplayName(), prompt.substring(0, Math.min(100, prompt.length())) + "...");
            return chatClient.prompt().user(prompt).call().content();
        } catch (Exception e) {
            log.error("LLM query error: {}", e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    @Override
    public String queryWithFunctions(String prompt, String... functions) {
        if (!available) {
            return "Mock response with functions: " + String.join(", ", functions);
        }
        try {
            log.debug("Executing query with functions on {}: {}", provider.getDisplayName(), String.join(", ", functions));
            Object[] tools = toolResolver.resolve(functions);
            if (tools.length == 0) {
                log.warn("No registered Spring AI tools resolved for {}. Falling back to plain query.", String.join(", ", functions));
                return query(prompt);
            }
            return chatClient.prompt().user(prompt).tools(tools).call().content();
        } catch (Exception e) {
            log.error("LLM query with functions error: {}. Falling back to plain query.", e.getMessage());
            return query(prompt);
        }
    }

    @Override
    public <T> T queryStructured(String prompt, Class<T> responseClass) {
        if (!available) {
            return MockResponseFactory.createMockResponse(responseClass, provider.getDisplayName());
        }
        try {
            log.debug("Executing structured query on {}: {}", provider.getDisplayName(), responseClass.getSimpleName());
            if (ScoringResult.class.equals(responseClass)) {
                String content = chatClient.prompt().user(prompt).call().content();
                return responseClass.cast(scoringResponseConverter.convert(content));
            }
            return chatClient.prompt().user(prompt).call().entity(responseClass);
        } catch (Exception e) {
            log.error("Structured LLM query error: {}", e.getMessage());
            throw new RuntimeException("Structured query error", e);
        }
    }

    @Override
    public LLMProvider getProvider() {
        return provider;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }
}
