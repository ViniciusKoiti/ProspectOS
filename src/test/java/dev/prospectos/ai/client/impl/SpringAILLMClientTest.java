package dev.prospectos.ai.client.impl;

import dev.prospectos.ai.client.LLMProvider;
import dev.prospectos.ai.dto.OutreachMessage;
import dev.prospectos.ai.dto.PriorityLevel;
import dev.prospectos.ai.dto.ScoringResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpringAILLMClientTest {

    @Test
    void queryAndFunctionsReturnMockContentWhenUnavailable() {
        SpringAILLMClient client = new SpringAILLMClient(null, LLMProvider.GROQ, false);

        assertThat(client.query("hello world")).startsWith("Mock response:");
        assertThat(client.queryWithFunctions("prompt", "scrapeWebsite")).contains("scrapeWebsite");
    }

    @Test
    void queryReturnsMockContentForEmptyPromptWhenUnavailable() {
        SpringAILLMClient client = new SpringAILLMClient(null, LLMProvider.GROQ, false);

        assertThat(client.query("")).isEqualTo("Mock response: ");
    }

    @Test
    void queryStructuredReturnsMockObjectsWhenUnavailable() {
        SpringAILLMClient client = new SpringAILLMClient(null, LLMProvider.OPENAI, false);

        ScoringResult scoring = client.queryStructured("prompt", ScoringResult.class);
        OutreachMessage outreach = client.queryStructured("prompt", OutreachMessage.class);

        assertThat(scoring.score()).isEqualTo(85);
        assertThat(scoring.priority()).isEqualTo(PriorityLevel.HOT);
        assertThat(outreach.subject()).contains("Performance optimization");
    }

    @Test
    void queryUsesChatClientWhenAvailable() {
        SpringAILLMClient client = new SpringAILLMClient(chatClient("response", null, null), LLMProvider.OPENAI, true);

        assertThat(client.query("hello")).isEqualTo("response");
    }

    @Test
    void queryWithFunctionsUsesChatClientWhenAvailable() {
        SpringAILLMClient client = new SpringAILLMClient(
            chatClient("function-response", null, null),
            LLMProvider.GROQ,
            true,
            new dev.prospectos.ai.client.LlmScoringResponseConverter(new dev.prospectos.ai.client.LlmStructuredResponseSanitizer()),
            new SpringAIToolResolver(Map.of("scrapeWebsite", (java.util.function.Function<String, String>) value -> value))
        );

        assertThat(client.queryWithFunctions("prompt", "scrapeWebsite"))
            .isEqualTo("function-response");
    }

    @Test
    void queryReturnsErrorMessageWhenChatClientFails() {
        SpringAILLMClient client = new SpringAILLMClient(chatClient(null, null, new RuntimeException("timeout")), LLMProvider.OPENAI, true);

        assertThat(client.query("hello")).isEqualTo("Error: timeout");
    }

    @Test
    void queryWithFunctionsReturnsErrorMessageWhenChatClientFails() {
        SpringAILLMClient client = new SpringAILLMClient(
            chatClient(null, null, new RuntimeException("boom")),
            LLMProvider.GROQ,
            true,
            new dev.prospectos.ai.client.LlmScoringResponseConverter(new dev.prospectos.ai.client.LlmStructuredResponseSanitizer()),
            new SpringAIToolResolver(Map.of("scrapeWebsite", (java.util.function.Function<String, String>) value -> value))
        );

        assertThat(client.queryWithFunctions("prompt", "scrapeWebsite")).isEqualTo("Error: boom");
    }

    @Test
    void queryWithFunctionsFallsBackToPlainQueryWhenToolsCannotBeResolved() {
        SpringAILLMClient client = new SpringAILLMClient(chatClient("plain-response", null, null), LLMProvider.GROQ, true);

        assertThat(client.queryWithFunctions("prompt", "scrapeWebsite")).isEqualTo("plain-response");
    }

    @Test
    void queryStructuredUsesScoringConverterWhenAvailable() {
        String rawJson = """
            {"score":91,"priority":"HOT","reasoning":"Great fit","breakdown":{"icpFit":30,"signals":20,"companySize":20,"timing":11,"accessibility":10},"recommendation":"Reach out"}
            """;
        SpringAILLMClient client = new SpringAILLMClient(chatClient(rawJson, null, null), LLMProvider.OPENAI, true);

        ScoringResult result = client.queryStructured("score", ScoringResult.class);

        assertThat(result.score()).isEqualTo(91);
        assertThat(result.priority()).isEqualTo(PriorityLevel.HOT);
        assertThat(result.reasoning()).isEqualTo("Great fit");
    }

    @Test
    void queryStructuredUsesEntityForNonScoringTypes() {
        OutreachMessage message = new OutreachMessage("Subject", "Body", "email", "consultative", new String[]{"CTA"});
        SpringAILLMClient client = new SpringAILLMClient(chatClient(null, message, null), LLMProvider.OPENAI, true);

        assertThat(client.queryStructured("prompt", OutreachMessage.class)).isSameAs(message);
    }

    @Test
    void queryStructuredWrapsFailuresWhenAvailable() {
        SpringAILLMClient client = new SpringAILLMClient(chatClient(null, null, new RuntimeException("bad response")), LLMProvider.OPENAI, true);

        assertThatThrownBy(() -> client.queryStructured("prompt", OutreachMessage.class))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Structured query error");
    }

    @Test
    void exposesProviderAndAvailability() {
        SpringAILLMClient unavailable = new SpringAILLMClient(null, LLMProvider.GROQ, false);
        SpringAILLMClient available = new SpringAILLMClient(null, LLMProvider.OPENAI, true);

        assertThat(unavailable.getProvider()).isEqualTo(LLMProvider.GROQ);
        assertThat(unavailable.isAvailable()).isFalse();
        assertThat(available.getProvider()).isEqualTo(LLMProvider.OPENAI);
        assertThat(available.isAvailable()).isTrue();
    }

    private ChatClient chatClient(String content, Object entity, RuntimeException error) {
        AtomicReference<String> promptRef = new AtomicReference<>();
        AtomicReference<Object[]> toolsRef = new AtomicReference<>();

        ChatClient chatClient = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec responseSpec = mock(ChatClient.CallResponseSpec.class);

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenAnswer(invocation -> {
            promptRef.set(invocation.getArgument(0, String.class));
            return requestSpec;
        });
        when(requestSpec.tools(any(Object[].class))).thenAnswer(invocation -> {
            toolsRef.set(invocation.getArgument(0, Object[].class));
            return requestSpec;
        });
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenAnswer(invocation -> {
            if (error != null) {
                throw error;
            }
            return content;
        });
        when(responseSpec.entity(any(Class.class))).thenAnswer(invocation -> {
            if (error != null) {
                throw error;
            }
            return entity;
        });

        return chatClient;
    }
}
