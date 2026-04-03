package dev.prospectos.infrastructure.service.prospect;

import java.net.URI;
import java.time.Duration;

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
class GooglePageSpeedAuditProviderTest {

    @Mock
    private RestTemplate restTemplate;

    @Test
    void auditMapsPerformanceScore() {
        var provider = createProvider(true, "key");
        var response = new GooglePageSpeedResponse(
            new GoogleLighthouseResult(new GooglePageSpeedCategories(new GooglePageSpeedCategory(0.42)))
        );
        when(restTemplate.getForObject(any(URI.class), eq(GooglePageSpeedResponse.class))).thenReturn(response);

        var result = provider.audit("https://acme.com");

        assertThat(result.available()).isTrue();
        assertThat(result.score()).isEqualTo(42);
        assertThat(result.findings()).containsExactly("PageSpeed indicates poor technical performance on the mobile audit.");

        var uriCaptor = ArgumentCaptor.forClass(URI.class);
        verify(restTemplate).getForObject(uriCaptor.capture(), eq(GooglePageSpeedResponse.class));
        assertThat(uriCaptor.getValue().toString())
            .contains("https://www.googleapis.com/pagespeedonline/v5/runPagespeed")
            .contains("url=https://acme.com")
            .contains("strategy=mobile")
            .contains("locale=pt-BR")
            .contains("key=key");
    }

    @Test
    void auditReturnsUnavailableWhenDisabled() {
        var provider = createProvider(false, "key");

        assertThat(provider.audit("https://acme.com").available()).isFalse();
        verifyNoInteractions(restTemplate);
    }

    @Test
    void auditThrowsWhenApiKeyIsMissing() {
        var provider = createProvider(true, " ");

        assertThatThrownBy(() -> provider.audit("https://acme.com"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("API key is required");
        verifyNoInteractions(restTemplate);
    }

    @Test
    void auditReturnsUnavailableWhenApiCallFails() {
        var provider = createProvider(true, "key");
        when(restTemplate.getForObject(any(URI.class), eq(GooglePageSpeedResponse.class)))
            .thenThrow(new RestClientException("boom"));

        assertThat(provider.audit("https://acme.com").available()).isFalse();
    }

    private GooglePageSpeedAuditProvider createProvider(boolean enabled, String apiKey) {
        var properties = new PageSpeedProperties(enabled, apiKey, null, "mobile", "pt-BR", Duration.ofSeconds(30));
        return new GooglePageSpeedAuditProvider(restTemplate, properties);
    }
}
