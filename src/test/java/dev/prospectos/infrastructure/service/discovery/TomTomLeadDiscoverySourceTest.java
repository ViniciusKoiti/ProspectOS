package dev.prospectos.infrastructure.service.discovery;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import dev.prospectos.api.dto.ICPDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TomTomLeadDiscoverySourceTest {

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private RestTemplate restTemplate;

    private TomTomLeadDiscoverySource source;

    private static final String TOMTOM_RESPONSE = """
        {
          "results": [
            {
              "poi": {
                "name": "Loja Central",
                "phone": "+55 11 3333-4444",
                "url": "www.lojacentral.com.br",
                "classifications": [
                  {
                    "names": [
                      { "name": "retail" }
                    ]
                  }
                ]
              },
              "address": {
                "freeformAddress": "Av Paulista, 1000, Sao Paulo, SP, Brasil"
              },
              "position": {
                "lat": -23.5505,
                "lon": -46.6333
              }
            },
            {
              "poi": {
                "name": "Cafe Litoral",
                "url": "https://cafelitoral.com"
              },
              "position": {
                "lat": -22.9068,
                "lon": -43.1729
              }
            }
          ]
        }
        """;

    private static final String TOMTOM_RESPONSE_THREE_RESULTS = """
        {
          "results": [
            { "poi": { "name": "Empresa A" }, "position": { "lat": -23.0, "lon": -46.0 } },
            { "poi": { "name": "Empresa B" }, "position": { "lat": -22.0, "lon": -45.0 } },
            { "poi": { "name": "Empresa C" }, "position": { "lat": -21.0, "lon": -44.0 } }
          ]
        }
        """;

    @BeforeEach
    void setUp() {
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        source = createSource(defaultProperties());
    }

    @Test
    void sourceNameShouldBeTomTom() {
        assertThat(source.sourceName()).isEqualTo("tomtom");
    }

    @Test
    void discoverShouldBuildOfficialTomTomRequestAndMapLocationFields() {
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(ResponseEntity.ok(TOMTOM_RESPONSE));

        DiscoveryContext context = new DiscoveryContext("lojas em sao paulo", "Founder", 5, mockIcp());
        List<DiscoveredLeadCandidate> results = source.discover(context);
        String requestUrl = captureDecodedRequestUrl();

        assertThat(requestUrl).contains("/search/2/poiSearch/lojas em sao paulo.json");
        assertThat(requestUrl).contains("key=test-api-key");
        assertThat(requestUrl).contains("limit=5");
        assertThat(requestUrl).contains("countrySet=BR");
        assertThat(requestUrl).contains("language=pt-BR");
        assertThat(requestUrl).contains("lat=-23.5505");
        assertThat(requestUrl).contains("lon=-46.6333");
        assertThat(requestUrl).contains("radius=10000");

        assertThat(results).hasSize(2);
        assertThat(results.getFirst().name()).isEqualTo("Loja Central");
        assertThat(results.getFirst().website()).isEqualTo("https://www.lojacentral.com.br");
        assertThat(results.getFirst().industry()).isEqualTo("retail");
        assertThat(results.getFirst().location()).isEqualTo("Av Paulista, 1000, Sao Paulo, SP, Brasil");
        assertThat(results.getFirst().contacts()).contains("+55 11 3333-4444");
        assertThat(results).allMatch(lead -> lead.sourceName().equals("tomtom"));
        assertThat(results.get(1).location()).isEqualTo("-22.906800,-43.172900");
    }

    @Test
    void discoverShouldRespectQuotaSafeLimit() {
        source = createSource(new TomTomProperties(
            "test-api-key",
            "https://api.tomtom.com",
            "pt-BR",
            2,
            10000,
            -23.5505,
            -46.6333,
            List.of("BR")
        ));
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(ResponseEntity.ok(TOMTOM_RESPONSE_THREE_RESULTS));

        DiscoveryContext context = new DiscoveryContext("tecnologia startup", "CTO", 10, mockIcp());
        List<DiscoveredLeadCandidate> results = source.discover(context);
        String requestUrl = captureDecodedRequestUrl();

        assertThat(requestUrl).contains("limit=2");
        assertThat(results).hasSize(2);
    }

    @Test
    void discoverShouldFallbackToMockWhenApiKeyIsMissing() {
        source = createSource(new TomTomProperties(
            "",
            "https://api.tomtom.com",
            "pt-BR",
            20,
            10000,
            -23.5505,
            -46.6333,
            List.of("BR")
        ));

        DiscoveryContext context = new DiscoveryContext("tecnologia startup", "CTO", 3, mockIcp());
        List<DiscoveredLeadCandidate> results = source.discover(context);

        verify(restTemplate, never()).getForEntity(anyString(), eq(String.class));
        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(lead -> lead.sourceName().equals("tomtom"));
    }

    @Test
    void discoverShouldFallbackWhenExternalClientFails() {
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenThrow(new RuntimeException("tomtom unavailable"));

        DiscoveryContext context = new DiscoveryContext("consulta desconhecida", "Gerente", 3, mockIcp());
        List<DiscoveredLeadCandidate> results = source.discover(context);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(lead -> lead.sourceName().equals("tomtom"));
    }

    private TomTomLeadDiscoverySource createSource(TomTomProperties properties) {
        return new TomTomLeadDiscoverySource(restTemplateBuilder, properties);
    }

    private TomTomProperties defaultProperties() {
        return new TomTomProperties(
            "test-api-key",
            "https://api.tomtom.com",
            "pt-BR",
            20,
            10000,
            -23.5505,
            -46.6333,
            List.of("BR")
        );
    }

    private String captureDecodedRequestUrl() {
        org.mockito.ArgumentCaptor<String> captor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(restTemplate).getForEntity(captor.capture(), eq(String.class));
        return URLDecoder.decode(captor.getValue(), StandardCharsets.UTF_8);
    }

    private ICPDto mockIcp() {
        return new ICPDto(1L, "ICP", "desc", List.of(), List.of("BR"), List.of(), null, null, List.of(), null);
    }
}
