package dev.prospectos.ai.client;

import dev.prospectos.ai.config.ScraperProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AIWebSearchScraperClientTest {

    private AIWebSearchScraperClient client;

    @AfterEach
    void tearDown() {
        if (client != null) {
            client.shutdownExecutor();
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
        assertThat(response.error()).isEqualTo("AI web search failed for https://acme.com after 2 attempts: boom");
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
        return stubChatClient(queue);
    }

    private ChatClient failingChatClient(RuntimeException... failures) {
        Queue<Object> queue = new ArrayDeque<>(List.of(failures));
        return stubChatClient(queue);
    }

    private ChatClient stubChatClient(Queue<Object> queue) {
        ChatClient chatClient = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec responseSpec = mock(ChatClient.CallResponseSpec.class);

        when(chatClient.prompt(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenAnswer(invocation -> {
            Object next = queue.remove();
            if (next instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            return next;
        });

        return chatClient;
    }
}
