package dev.prospectos.ai.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for ScraperClient implementations.
 * Verifies that the correct implementation is selected based on profile and configuration.
 */
@SpringBootTest
@ActiveProfiles("test")
class ScraperClientTest {

    @Autowired
    private ScraperClientInterface scraperClient;

    @Test
    void shouldInjectMockScraperClientInTestProfile() {
        assertThat(scraperClient).isNotNull();
        assertThat(scraperClient).isInstanceOf(MockScraperClient.class);
    }

    @Test
    void shouldScrapeWebsiteSuccessfully() {
        ScrapingResponse response = scraperClient.scrapeWebsiteSync("https://example.com", false);

        assertThat(response.success()).isTrue();
        assertThat(response.data()).isNotNull();
        assertThat(response.data()).containsKey("company_name");
        assertThat(response.data()).containsKey("emails");
        assertThat(response.error()).isNull();
    }

    @Test
    void shouldSearchNewsSuccessfully() {
        NewsResponse response = scraperClient.searchNews("Test Company", 30);

        assertThat(response.news()).isNotNull();
        assertThat(response.news()).isNotEmpty();
        assertThat(response.news().get(0)).contains("Test Company");
    }
}