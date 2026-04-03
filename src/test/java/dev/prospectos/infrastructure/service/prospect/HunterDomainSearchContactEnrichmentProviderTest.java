package dev.prospectos.infrastructure.service.prospect;

import java.net.URI;
import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
class HunterDomainSearchContactEnrichmentProviderTest {

    @Mock
    private RestTemplate restTemplate;

    @Test
    void findsContactsFromWebsiteDomain() {
        var provider = createProvider(true, "key");
        var response = new HunterDomainSearchResponse(new HunterDomainSearchData(List.of(
            new HunterEmailEntry("owner@acme.com", "Owner", "Acme", "Founder", 91)
        )));
        when(restTemplate.getForObject(any(URI.class), eq(HunterDomainSearchResponse.class))).thenReturn(response);

        var contacts = provider.findContacts("https://www.acme.com/about");

        assertThat(contacts).hasSize(1);
        assertThat(contacts.getFirst().email()).isEqualTo("owner@acme.com");

        var uriCaptor = ArgumentCaptor.forClass(URI.class);
        verify(restTemplate).getForObject(uriCaptor.capture(), eq(HunterDomainSearchResponse.class));
        assertThat(uriCaptor.getValue().toString())
            .contains("https://api.hunter.io/v2/domain-search")
            .contains("domain=acme.com")
            .contains("api_key=key")
            .contains("limit=5");
    }

    @Test
    void returnsEmptyWhenDisabled() {
        var provider = createProvider(false, "key");

        assertThat(provider.findContacts("https://acme.com")).isEmpty();
        verifyNoInteractions(restTemplate);
    }

    @Test
    void throwsWhenApiKeyIsMissing() {
        var provider = createProvider(true, " ");

        assertThatThrownBy(() -> provider.findContacts("https://acme.com"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("API key is required");
        verifyNoInteractions(restTemplate);
    }

    @Test
    void returnsEmptyWhenCallFails() {
        var provider = createProvider(true, "key");
        when(restTemplate.getForObject(any(URI.class), eq(HunterDomainSearchResponse.class)))
            .thenThrow(new RestClientException("boom"));

        assertThat(provider.findContacts("https://acme.com")).isEmpty();
    }

    private HunterDomainSearchContactEnrichmentProvider createProvider(boolean enabled, String apiKey) {
        var properties = new HunterProperties(enabled, apiKey, null, Duration.ofSeconds(15), 5);
        return new HunterDomainSearchContactEnrichmentProvider(
            restTemplate,
            properties,
            new WebsiteDomainExtractor(),
            new HunterResponseMapper()
        );
    }
}
