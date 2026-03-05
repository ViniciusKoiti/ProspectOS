package dev.prospectos.ai.client;

import dev.prospectos.ai.config.ScraperProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static org.assertj.core.api.Assertions.assertThat;

class AIWebSearchScraperClientTest {

    private AIWebSearchScraperClient client;

    @AfterEach
    void tearDown() {
        if (client != null) {
            ((java.util.concurrent.ExecutorService) ReflectionTestUtils.getField(client, "executorService")).shutdownNow();
        }
    }

    @Test
    void scrapeWebsiteSyncReturnsConvertedData() {
        client = new AIWebSearchScraperClient(
            chatClient("""
                {
                  "company_name": "Acme",
                  "description": "Cloud platform",
                  "emails": ["ceo@acme.com"],
                  "phone": "123",
                  "technologies": ["Java"],
                  "industry": "Software",
                  "size": "SMALL",
                  "recent_news": ["Raised funds"]
                }
                """),
            scraperProperties(0, false),
            new LlmScrapingResponseConverter()
        );

        ScrapingResponse response = client.scrapeWebsiteSync("https://acme.com", false);

        assertThat(response.success()).isTrue();
        assertThat(response.data()).containsEntry("company_name", "Acme");
        assertThat(response.data()).containsEntry("source", "ai_web_search");
    }

    @Test
    void scrapeWebsiteSyncRetriesAndReturnsErrorAfterExhaustion() {
        client = new AIWebSearchScraperClient(
            failingChatClient(new RuntimeException("boom"), new RuntimeException("boom")),
            scraperProperties(1, false),
            new LlmScrapingResponseConverter()
        );

        ScrapingResponse response = client.scrapeWebsiteSync("https://acme.com", false);

        assertThat(response.success()).isFalse();
        assertThat(response.error()).contains("after 2 attempts");
        assertThat(response.error()).contains("boom");
    }

    @Test
    void searchNewsParsesRelevantLinesAndFiltersPreamble() {
        client = new AIWebSearchScraperClient(
            chatClient("""
                Here are the most relevant updates:
                - Acme raised a Series A round.
                - Acme launched a new AI product.
                tiny
                """),
            scraperProperties(0, true),
            new LlmScrapingResponseConverter()
        );

        NewsResponse response = client.searchNews("Acme", 30);

        assertThat(response.news())
            .containsExactly(
                "Acme raised a Series A round.",
                "Acme launched a new AI product."
            );
    }

    private ScraperProperties scraperProperties(int retries, boolean deepSearchEnabled) {
        return new ScraperProperties(
            true,
            new ScraperProperties.Ai(Duration.ofSeconds(1), retries, deepSearchEnabled, Duration.ofHours(1)),
            new ScraperProperties.Service("http://localhost:3000", Duration.ofSeconds(1), 1)
        );
    }

    private ChatClient chatClient(String... contents) {
        Queue<Object> queue = new ArrayDeque<>(List.of(contents));
        return proxyClient(queue);
    }

    private ChatClient failingChatClient(RuntimeException... failures) {
        Queue<Object> queue = new ArrayDeque<>(List.of(failures));
        return proxyClient(queue);
    }

    private ChatClient proxyClient(Queue<Object> queue) {
        Object responseSpec = Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class[]{ChatClient.CallResponseSpec.class},
            (proxy, method, args) -> {
                if ("content".equals(method.getName())) {
                    Object next = queue.remove();
                    if (next instanceof RuntimeException runtimeException) {
                        throw runtimeException;
                    }
                    return next;
                }
                return null;
            }
        );

        Object requestSpec = Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class[]{ChatClient.ChatClientRequestSpec.class},
            (proxy, method, args) -> switch (method.getName()) {
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
