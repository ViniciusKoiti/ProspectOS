package dev.prospectos.infrastructure.service.discovery;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    @BeforeEach
    void setUp() {
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        source = new TomTomLeadDiscoverySource(restTemplateBuilder, "tomtom-test-key");
    }

    @Test
    void sourceNameShouldBeTomTom() {
        assertThat(source.sourceName()).isEqualTo("tomtom");
    }

    @Test
    void discoverShouldReturnCandidatesWithTomTomSourceName() {
        DiscoveryContext context = new DiscoveryContext("tecnologia startup", "CTO", 5, null);
        when(restTemplate.getForEntity(
            eq("https://api.tomtom.com/search/2/poiSearch/tecnologia+startup.json?key=tomtom-test-key&limit=5&countrySet=BR"),
            eq(Map.class)
        )).thenReturn(ResponseEntity.ok(payload(
            result("Loja Digital Alpha", "https://alpha.com", "+55 11 99999-0001", "Sao Paulo, SP")
        )));

        List<DiscoveredLeadCandidate> results = source.discover(context);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(lead -> lead.sourceName().equals("tomtom"));
        assertThat(results.getFirst().name()).isEqualTo("Loja Digital Alpha");
        assertThat(results.getFirst().contacts()).contains("+55 11 99999-0001");
    }

    @Test
    void discoverShouldRespectLimit() {
        DiscoveryContext context = new DiscoveryContext("tecnologia startup", "CTO", 2, null);
        when(restTemplate.getForEntity(
            eq("https://api.tomtom.com/search/2/poiSearch/tecnologia+startup.json?key=tomtom-test-key&limit=2&countrySet=BR"),
            eq(Map.class)
        )).thenReturn(ResponseEntity.ok(payload(
            result("Empresa 1", "https://empresa1.com.br", "+55 11 11111-1111", "Campinas, SP"),
            result("Empresa 2", "https://empresa2.com.br", "+55 11 22222-2222", "Campinas, SP"),
            result("Empresa 3", "https://empresa3.com.br", "+55 11 33333-3333", "Campinas, SP")
        )));

        List<DiscoveredLeadCandidate> results = source.discover(context);

        assertThat(results).hasSize(2);
    }

    @Test
    void discoverShouldFallbackWhenExternalClientFails() {
        when(restTemplate.getForEntity(anyString(), eq(Map.class))).thenThrow(new RuntimeException("tomtom unavailable"));

        DiscoveryContext context = new DiscoveryContext("consulta desconhecida", "Gerente", 3, null);

        List<DiscoveredLeadCandidate> results = source.discover(context);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(lead -> lead.sourceName().equals("tomtom"));
    }

    @Test
    void discoverShouldFallbackWithoutApiKey() {
        TomTomLeadDiscoverySource noKeySource = new TomTomLeadDiscoverySource(restTemplateBuilder, null);
        DiscoveryContext context = new DiscoveryContext("restaurante", "Owner", 2, null);

        List<DiscoveredLeadCandidate> results = noKeySource.discover(context);

        assertThat(results).isNotEmpty();
        verify(restTemplate, never()).getForEntity(anyString(), eq(Map.class));
    }

    private Map<String, Object> payload(Map<String, Object>... results) {
        return new LinkedHashMap<>(Map.of("results", List.of(results)));
    }

    private Map<String, Object> result(String name, String website, String phone, String freeformAddress) {
        Map<String, Object> poi = new LinkedHashMap<>();
        poi.put("name", name);
        poi.put("url", website);
        poi.put("phone", phone);
        poi.put("categories", List.of("business-services"));

        Map<String, Object> address = new LinkedHashMap<>();
        address.put("freeformAddress", freeformAddress);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("poi", poi);
        result.put("address", address);
        return result;
    }
}
