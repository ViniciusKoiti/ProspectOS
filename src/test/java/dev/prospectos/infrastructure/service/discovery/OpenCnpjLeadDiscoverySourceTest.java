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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenCnpjLeadDiscoverySourceTest {

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private RestTemplate restTemplate;

    private OpenCnpjLeadDiscoverySource source;

    @BeforeEach
    void setUp() {
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        source = new OpenCnpjLeadDiscoverySource(restTemplateBuilder);
    }

    @Test
    void sourceName_shouldBeOpenCnpj() {
        assertThat(source.sourceName()).isEqualTo("open-cnpj");
    }

    @Test
    void discover_shouldReturnCandidatesWithOpenCnpjSourceName() {
        DiscoveryContext context = new DiscoveryContext("tecnologia startup", "CTO", 5, null);

        List<DiscoveredLeadCandidate> results = source.discover(context);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(candidate -> candidate.sourceName().equals("open-cnpj"));
    }

    @Test
    void discover_shouldRespectLimit() {
        DiscoveryContext context = new DiscoveryContext("tecnologia", "CTO", 2, null);

        List<DiscoveredLeadCandidate> results = source.discover(context);

        assertThat(results).hasSizeLessThanOrEqualTo(2);
    }

    @Test
    void discover_shouldFallbackWhenExternalClientFails() {
        when(restTemplate.getForEntity(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.eq(String.class)))
            .thenThrow(new IllegalStateException("boom"));

        DiscoveryContext context = new DiscoveryContext("consultoria", "Diretor", 3, null);

        List<DiscoveredLeadCandidate> results = source.discover(context);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(candidate -> candidate.sourceName().equals("open-cnpj"));
    }
}
