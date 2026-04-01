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
class GooglePlacesLeadDiscoverySourceTest {

    @Mock
    private RestTemplate restTemplate;

    @Test
    void discoverMapsGooglePlacesResponseIntoCandidate() {
        var source = createSource(true, "test-key");
        var payload = new GooglePlacesSearchTextResponse(List.of(
            new GooglePlace(
                new GoogleDisplayName("Smile Studio"),
                "Orlando, FL, USA",
                "https://smilestudio.example.com",
                "+1 407-555-0101",
                List.of("dentist", "health")
            )
        ));
        when(restTemplate.postForEntity(any(URI.class), any(HttpEntity.class), eq(GooglePlacesSearchTextResponse.class)))
            .thenReturn(ResponseEntity.ok(payload));

        var results = source.discover(new DiscoveryContext("dentists in orlando", null, 5, null));

        assertThat(results).hasSize(1);
        var candidate = results.getFirst();
        assertThat(candidate.name()).isEqualTo("Smile Studio");
        assertThat(candidate.website()).isEqualTo("https://smilestudio.example.com");
        assertThat(candidate.contacts()).containsExactly("+1 407-555-0101");
        assertThat(candidate.location()).isEqualTo("Orlando, FL, USA");
        assertThat(candidate.industry()).isEqualTo("dentist");
        assertThat(candidate.sourceName()).isEqualTo("google-places");

        var uriCaptor = ArgumentCaptor.forClass(URI.class);
        verify(restTemplate).postForEntity(uriCaptor.capture(), any(HttpEntity.class), eq(GooglePlacesSearchTextResponse.class));
        assertThat(uriCaptor.getValue().toString()).isEqualTo("https://places.googleapis.com/v1/places:searchText");
    }

    @Test
    void discoverReturnsEmptyWhenSourceDisabled() {
        var source = createSource(false, "test-key");

        assertThat(source.discover(new DiscoveryContext("query", null, 3, null))).isEmpty();
        verifyNoInteractions(restTemplate);
    }

    @Test
    void discoverThrowsWhenApiKeyIsMissing() {
        var source = createSource(true, " ");

        assertThatThrownBy(() -> source.discover(new DiscoveryContext("query", null, 3, null)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("API key is required");
        verifyNoInteractions(restTemplate);
    }

    @Test
    void discoverReturnsEmptyWhenApiCallFails() {
        var source = createSource(true, "test-key");
        when(restTemplate.postForEntity(any(URI.class), any(HttpEntity.class), eq(GooglePlacesSearchTextResponse.class)))
            .thenThrow(new RestClientException("boom"));

        assertThat(source.discover(new DiscoveryContext("query", null, 3, null))).isEmpty();
    }

    private GooglePlacesLeadDiscoverySource createSource(boolean enabled, String apiKey) {
        var properties = new GooglePlacesProperties(enabled, apiKey, "pt-BR", 20, null);
        return new GooglePlacesLeadDiscoverySource(restTemplate, properties, new GooglePlacesResponseMapper("google-places"));
    }
}
