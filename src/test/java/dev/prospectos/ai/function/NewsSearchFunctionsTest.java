package dev.prospectos.ai.function;

import dev.prospectos.ai.client.NewsResponse;
import dev.prospectos.ai.client.ScraperClientInterface;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NewsSearchFunctionsTest {

    @Test
    void searchCompanyNewsDelegatesToScraperClient() {
        ScraperClientInterface scraperClient = mock(ScraperClientInterface.class);
        when(scraperClient.searchNews("Acme", 7)).thenReturn(new NewsResponse(List.of("Raised funds")));
        NewsSearchFunctions functions = new NewsSearchFunctions(scraperClient);

        List<String> result = functions.searchCompanyNews().apply(new NewsSearchFunctions.NewsRequest("Acme", 7));

        assertThat(result).containsExactly("Raised funds");
        verify(scraperClient).searchNews("Acme", 7);
        assertThat(new NewsSearchFunctions.NewsRequest("Acme").daysBack()).isEqualTo(30);
    }
}
