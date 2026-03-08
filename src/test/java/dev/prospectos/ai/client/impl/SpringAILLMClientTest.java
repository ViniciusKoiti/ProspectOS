package dev.prospectos.ai.client.impl;

import dev.prospectos.ai.client.LLMProvider;
import dev.prospectos.ai.dto.OutreachMessage;
import dev.prospectos.ai.dto.PriorityLevel;
import dev.prospectos.ai.dto.ScoringResult;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        SpringAILLMClient client = new SpringAILLMClient(chatClient("function-response", null, null), LLMProvider.GROQ, true);

        assertThat(client.queryWithFunctions("prompt", "scrapeWebsite", "searchCompanyNews"))
            .isEqualTo("function-response");
    }

    @Test
    void queryReturnsErrorMessageWhenChatClientFails() {
        SpringAILLMClient client = new SpringAILLMClient(chatClient(null, null, new RuntimeException("timeout")), LLMProvider.OPENAI, true);

        assertThat(client.query("hello")).isEqualTo("Error: timeout");
    }

    @Test
    void queryWithFunctionsReturnsErrorMessageWhenChatClientFails() {
        SpringAILLMClient client = new SpringAILLMClient(chatClient(null, null, new RuntimeException("boom")), LLMProvider.GROQ, true);

        assertThat(client.queryWithFunctions("prompt", "scrapeWebsite")).isEqualTo("Error: boom");
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

        Object responseSpec = Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class[]{ChatClient.CallResponseSpec.class},
            (proxy, method, args) -> {
                if (error != null) {
                    throw error;
                }
                return switch (method.getName()) {
                    case "content" -> content;
                    case "entity" -> entity;
                    default -> null;
                };
            }
        );

        Object requestSpec = Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class[]{ChatClient.ChatClientRequestSpec.class},
            (proxy, method, args) -> switch (method.getName()) {
                case "user" -> {
                    promptRef.set((String) args[0]);
                    yield proxy;
                }
                case "tools" -> proxy;
                case "call" -> responseSpec;
                case "mutate" -> null;
                default -> proxy;
            }
        );

        return (ChatClient) Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class[]{ChatClient.class},
            (proxy, method, args) -> switch (method.getName()) {
                case "prompt" -> requestSpec;
                case "mutate" -> null;
                default -> null;
            }
        );
    }
}
