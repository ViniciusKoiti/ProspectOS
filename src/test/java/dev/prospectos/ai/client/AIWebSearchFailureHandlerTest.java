package dev.prospectos.ai.client;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

class AIWebSearchFailureHandlerTest {

    private final AIWebSearchFailureHandler handler = new AIWebSearchFailureHandler();

    @Test
    void scrapingFailureUsesTimeoutSpecificMessage() {
        ScrapingResponse response = handler.scrapingFailure("https://acme.com", 3, new TimeoutException("timed out"));

        assertThat(response.success()).isFalse();
        assertThat(response.error()).isEqualTo("AI web search timed out for https://acme.com after 3 attempts");
    }

    @Test
    void newsFailureUnwrapsExecutionExceptionCause() {
        NewsResponse response = handler.newsFailure(
            "Acme",
            2,
            new ExecutionException(new IllegalStateException("provider unavailable"))
        );

        assertThat(response.news()).containsExactly("AI news search failed for Acme after 2 attempts: provider unavailable");
    }
}
