package dev.prospectos.infrastructure.service.discovery;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
        source = new TomTomLeadDiscoverySource(restTemplateBuilder);
    }

    @Test
    void sourceNameShouldBeTomTom() {
        assertThat(source.sourceName()).isEqualTo("tomtom");
    }

    @Test
    void discoverShouldReturnCandidatesWithTomTomSourceName() {
        DiscoveryContext context = new DiscoveryContext("tecnologia startup", "CTO", 5, null);

        List<DiscoveredLeadCandidate> results = source.discover(context);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(lead -> lead.sourceName().equals("tomtom"));
    }

    @Test
    void discoverShouldRespectLimit() {
        DiscoveryContext context = new DiscoveryContext("tecnologia startup", "CTO", 2, null);

        List<DiscoveredLeadCandidate> results = source.discover(context);

        assertThat(results).hasSize(2);
    }

    @Test
    void discoverShouldFallbackWhenExternalClientFails() {
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenThrow(new RuntimeException("tomtom unavailable"));

        DiscoveryContext context = new DiscoveryContext("consulta desconhecida", "Gerente", 3, null);

        List<DiscoveredLeadCandidate> results = source.discover(context);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(lead -> lead.sourceName().equals("tomtom"));
    }
}
