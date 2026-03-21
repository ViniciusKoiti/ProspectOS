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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
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
        when(restTemplate.getForEntity(eq("https://opencnpj.com/api/companies?query=tecnologia+startup&limit=5"), eq(Map.class)))
            .thenReturn(ResponseEntity.ok(payload(
                company("Empresa Real Tech", "https://real.tech", "Tecnologia", "Sao Paulo", "SP", "contato@real.tech")
            )));

        List<DiscoveredLeadCandidate> results = source.discover(context);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(candidate -> candidate.sourceName().equals("open-cnpj"));
        assertThat(results.getFirst().name()).isEqualTo("Empresa Real Tech");
        assertThat(results.getFirst().website()).isEqualTo("https://real.tech");
    }

    @Test
    void discover_shouldRespectLimit() {
        DiscoveryContext context = new DiscoveryContext("tecnologia", "CTO", 2, null);
        when(restTemplate.getForEntity(eq("https://opencnpj.com/api/companies?query=tecnologia&limit=2"), eq(Map.class)))
            .thenReturn(ResponseEntity.ok(payload(
                company("Empresa 1", "https://empresa1.com.br", "Tecnologia", "Campinas", "SP", "contato1@empresa.com"),
                company("Empresa 2", "https://empresa2.com.br", "Tecnologia", "Campinas", "SP", "contato2@empresa.com"),
                company("Empresa 3", "https://empresa3.com.br", "Tecnologia", "Campinas", "SP", "contato3@empresa.com")
            )));

        List<DiscoveredLeadCandidate> results = source.discover(context);

        assertThat(results).hasSize(2);
    }

    @Test
    void discover_shouldFallbackWhenExternalClientFails() {
        when(restTemplate.getForEntity(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.eq(Map.class)))
            .thenThrow(new IllegalStateException("boom"));

        DiscoveryContext context = new DiscoveryContext("consultoria", "Diretor", 3, null);

        List<DiscoveredLeadCandidate> results = source.discover(context);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(candidate -> candidate.sourceName().equals("open-cnpj"));
    }

    @Test
    void discover_shouldUseEncodedQueryInExternalUrl() {
        String query = "consultoria digital";
        DiscoveryContext context = new DiscoveryContext(query, "Diretor", 2, null);
        when(restTemplate.getForEntity(eq("https://opencnpj.com/api/companies?query=consultoria+digital&limit=2"), eq(Map.class)))
            .thenReturn(ResponseEntity.ok(payload(company("Empresa URL", null, "Consultoria", "Curitiba", "PR", "contato@empresaurl.com"))));

        source.discover(context);

        verify(restTemplate).getForEntity("https://opencnpj.com/api/companies?query=consultoria+digital&limit=2", Map.class);
    }

    private Map<String, Object> payload(Map<String, Object>... companies) {
        return new LinkedHashMap<>(Map.of("results", List.of(companies)));
    }

    private Map<String, Object> company(
        String name,
        String website,
        String industry,
        String city,
        String state,
        String email
    ) {
        Map<String, Object> company = new LinkedHashMap<>();
        company.put("razao_social", name);
        company.put("website", website);
        company.put("email", email);
        company.put("municipio", city);
        company.put("uf", state);
        company.put("atividade_principal", industry);
        return company;
    }
}
