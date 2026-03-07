package dev.prospectos.ai.function;

import dev.prospectos.ai.client.ScraperClientInterface;
import dev.prospectos.ai.client.ScrapingResponse;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScrapingFunctionsTest {

    @Test
    void scrapeWebsiteReturnsScrapedDataOnSuccess() {
        ScraperClientInterface scraperClient = mock(ScraperClientInterface.class);
        when(scraperClient.scrapeWebsiteSync("https://acme.com", true))
            .thenReturn(new ScrapingResponse(true, Map.of("company_name", "Acme"), null));
        ScrapingFunctions functions = new ScrapingFunctions(scraperClient);

        Map<String, Object> result = functions.scrapeWebsite().apply(new ScrapingFunctions.ScrapingRequest("https://acme.com", true));

        assertThat(result).containsEntry("company_name", "Acme");
        verify(scraperClient).scrapeWebsiteSync("https://acme.com", true);
    }

    @Test
    void scrapeWebsiteReturnsErrorPayloadOnFailure() {
        ScraperClientInterface scraperClient = mock(ScraperClientInterface.class);
        when(scraperClient.scrapeWebsiteSync("https://acme.com", false))
            .thenReturn(new ScrapingResponse(false, null, "timeout"));
        ScrapingFunctions functions = new ScrapingFunctions(scraperClient);

        Map<String, Object> result = functions.scrapeWebsite().apply(new ScrapingFunctions.ScrapingRequest("https://acme.com", false));

        assertThat(result).containsEntry("error", "timeout");
        assertThat(result).containsEntry("success", false);
    }
}
