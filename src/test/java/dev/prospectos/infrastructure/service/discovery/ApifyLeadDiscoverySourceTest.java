package dev.prospectos.infrastructure.service.discovery;

import java.net.URI;
import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApifyLeadDiscoverySourceTest {

    @Mock
    private RestTemplate restTemplate;

    @Test
    void discoverMapsApifyResponseIntoCandidate() {
        var source = createSource(true, "token");
        var payload = new ApifyDatasetItem[] {
            new ApifyDatasetItem(
                "Coffee Spot",
                null,
                "Orlando, FL, USA",
                null,
                "https://coffee.example.com",
                "+1 407-555-0001",
                null,
                "Cafe",
                null,
                List.of()
            )
        };
        when(restTemplate.postForEntity(any(URI.class), any(HttpEntity.class), eq(ApifyDatasetItem[].class)))
            .thenReturn(ResponseEntity.ok(payload));

        var results = source.discover(new DiscoveryContext("coffee shops in Orlando", null, 5, null));

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().name()).isEqualTo("Coffee Spot");

        var uriCaptor = ArgumentCaptor.forClass(URI.class);
        verify(restTemplate).postForEntity(uriCaptor.capture(), any(HttpEntity.class), eq(ApifyDatasetItem[].class));
        assertThat(uriCaptor.getValue().toString())
            .isEqualTo("https://api.apify.com/v2/acts/scraper-engine~google-maps-scraper/run-sync-get-dataset-items");
    }

    @Test
    void discoverReturnsEmptyWhenSourceDisabled() {
        var source = createSource(false, "token");

        assertThat(source.discover(new DiscoveryContext("query", null, 3, null))).isEmpty();
        verifyNoInteractions(restTemplate);
    }

    @Test
    void discoverThrowsWhenTokenIsMissing() {
        var source = createSource(true, " ");

        assertThatThrownBy(() -> source.discover(new DiscoveryContext("query", null, 3, null)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("API token is required");
        verifyNoInteractions(restTemplate);
    }

    @Test
    void discoverReturnsEmptyWhenApiCallFails() {
        var source = createSource(true, "token");
        when(restTemplate.postForEntity(any(URI.class), any(HttpEntity.class), eq(ApifyDatasetItem[].class)))
            .thenThrow(new RestClientException("boom"));

        assertThat(source.discover(new DiscoveryContext("query", null, 3, null))).isEmpty();
    }

    private ApifyLeadDiscoverySource createSource(boolean enabled, String token) {
        var properties = new ApifyProperties(enabled, token, null, null, Duration.ofSeconds(45), false);
        return new ApifyLeadDiscoverySource(restTemplate, properties, new ApifyResponseMapper("apify"));
    }
}
