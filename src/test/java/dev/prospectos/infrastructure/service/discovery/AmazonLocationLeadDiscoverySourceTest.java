package dev.prospectos.infrastructure.service.discovery;

import java.net.URI;
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
class AmazonLocationLeadDiscoverySourceTest {

    @Mock
    private RestTemplate restTemplate;

    @Test
    void discoverMapsSearchTextResponseIntoCandidate() {
        AmazonLocationLeadDiscoverySource source = createSource(true, "test-key");
        AmazonLocationSearchTextResponse payload = new AmazonLocationSearchTextResponse(List.of(
            new AmazonLocationResultItem(
                "Amazon YVR11",
                "PointOfInterest",
                new AmazonLocationAddress(
                    "Amazon YVR11, 510 W Georgia St, Vancouver, BC, Canada",
                    "Vancouver",
                    "Downtown Vancouver",
                    null,
                    new AmazonLocationCountry("CA", "CAN", "Canada")
                ),
                List.of(new AmazonLocationCategory("Business Facility")),
                new AmazonLocationContacts(
                    List.of(new AmazonLocationContactValue("contact@amazon-yvr11.example")),
                    List.of(new AmazonLocationContactValue("www.amazon-yvr11.example"))
                )
            )
        ));
        when(restTemplate.postForEntity(any(URI.class), any(HttpEntity.class), eq(AmazonLocationSearchTextResponse.class)))
            .thenReturn(ResponseEntity.ok(payload));

        List<DiscoveredLeadCandidate> results = source.discover(new DiscoveryContext("amazon office", null, 5, null));

        assertThat(results).hasSize(1);
        DiscoveredLeadCandidate candidate = results.getFirst();
        assertThat(candidate.name()).isEqualTo("Amazon YVR11");
        assertThat(candidate.website()).isEqualTo("https://www.amazon-yvr11.example");
        assertThat(candidate.industry()).isEqualTo("business facility");
        assertThat(candidate.location()).isEqualTo("Vancouver, CA");
        assertThat(candidate.contacts()).containsExactly("contact@amazon-yvr11.example");
        assertThat(candidate.sourceName()).isEqualTo("amazon-location");

        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        verify(restTemplate).postForEntity(uriCaptor.capture(), any(HttpEntity.class), eq(AmazonLocationSearchTextResponse.class));
        assertThat(uriCaptor.getValue().toString())
            .contains("https://places.geo.us-east-1.amazonaws.com/v2/search-text")
            .contains("key=test-key");
    }

    @Test
    void discoverReturnsEmptyWhenSourceDisabled() {
        AmazonLocationLeadDiscoverySource source = createSource(false, "test-key");
        assertThat(source.discover(new DiscoveryContext("query", null, 3, null))).isEmpty();
        verifyNoInteractions(restTemplate);
    }

    @Test
    void discoverThrowsWhenApiKeyIsMissing() {
        AmazonLocationLeadDiscoverySource source = createSource(true, " ");
        assertThatThrownBy(() -> source.discover(new DiscoveryContext("query", null, 3, null)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("API key is required");
        verifyNoInteractions(restTemplate);
    }

    @Test
    void discoverReturnsEmptyWhenApiCallFails() {
        AmazonLocationLeadDiscoverySource source = createSource(true, "test-key");
        when(restTemplate.postForEntity(any(URI.class), any(HttpEntity.class), eq(AmazonLocationSearchTextResponse.class)))
            .thenThrow(new RestClientException("boom"));

        assertThat(source.discover(new DiscoveryContext("query", null, 3, null))).isEmpty();
    }

    private AmazonLocationLeadDiscoverySource createSource(boolean enabled, String apiKey) {
        AmazonLocationProperties properties = new AmazonLocationProperties(
            enabled, "us-east-1", apiKey, "pt-BR", "SingleUse", 20, List.of("BRA")
        );
        return new AmazonLocationLeadDiscoverySource(restTemplate, properties, new AmazonLocationResponseMapper("amazon-location"));
    }
}
