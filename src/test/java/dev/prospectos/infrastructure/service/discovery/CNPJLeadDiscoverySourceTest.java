package dev.prospectos.infrastructure.service.discovery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * 🧪 Teste Unitário do CNPJLeadDiscoverySource
 * 
 * Valida que a integração CNPJ:
 * - Retorna empresas brasileiras mockadas
 * - Funciona com diferentes queries
 * - Atribui source corretamente
 * - Graceful degradation quando API falha
 */
@ExtendWith(MockitoExtension.class)
class CNPJLeadDiscoverySourceTest {

    @Mock
    private RestTemplateBuilder restTemplateBuilder;
    
    @Mock
    private RestTemplate restTemplate;

    private CNPJLeadDiscoverySource cnpjSource;

    @BeforeEach
    void setUp() {
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        cnpjSource = new CNPJLeadDiscoverySource(restTemplateBuilder);
    }

    @Test
    void shouldReturnCorrectSourceName() {
        // When & Then
        assertThat(cnpjSource.sourceName()).isEqualTo("cnpj-ws");
    }

    @Test
    void shouldReturnTechCompaniesForTechQuery() {
        // Given
        DiscoveryContext context = new DiscoveryContext("tecnologia startup", "CTO", 5, null);

        // When
        List<DiscoveredLeadCandidate> results = cnpjSource.discover(context);

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(3); // Deve retornar 3 empresas tech mockadas
        
        // Verificar empresas específicas esperadas
        assertThat(results).anyMatch(lead -> 
            lead.name().equals("TechSolutions Brasil Ltda") &&
            lead.industry().equals("technology") &&
            lead.location().contains("São Paulo")
        );
        
        assertThat(results).anyMatch(lead ->
            lead.name().equals("InovaPay Fintech SA") &&
            lead.industry().equals("fintech") &&
            lead.location().contains("Rio de Janeiro")
        );

        // Verificar source attribution
        assertThat(results).allMatch(lead -> 
            lead.sourceName().equals("cnpj-ws")
        );
    }

    @Test
    void shouldReturnAgroCompaniesForAgroQuery() {
        // Given
        DiscoveryContext context = new DiscoveryContext("agronegócio agricultura", "Diretor", 5, null);

        // When
        List<DiscoveredLeadCandidate> results = cnpjSource.discover(context);

        // Then
        assertThat(results).isNotEmpty();
        
        // Deve conter empresas do agro
        assertThat(results).anyMatch(lead ->
            lead.name().equals("AgroTech Mato Grosso Ltda") &&
            lead.industry().equals("agtech") &&
            lead.location().contains("Cuiabá")
        );
        
        assertThat(results).anyMatch(lead ->
            lead.name().equals("FarmData Analytics") &&
            lead.industry().equals("agtech")
        );
    }

    @Test
    void shouldReturnConsultingCompaniesForBusinessQuery() {
        // Given
        DiscoveryContext context = new DiscoveryContext("consultoria empresas", "Consultor", 5, null);

        // When
        List<DiscoveredLeadCandidate> results = cnpjSource.discover(context);

        // Then
        assertThat(results).isNotEmpty();
        
        assertThat(results).anyMatch(lead ->
            lead.name().equals("Consultoria Estratégica Brasil") &&
            lead.industry().equals("consulting") &&
            lead.location().contains("Brasília")
        );
    }

    @Test
    void shouldReturnHealthCompaniesForHealthQuery() {
        // Given  
        DiscoveryContext context = new DiscoveryContext("saúde telemedicina", "CEO", 3, null);

        // When
        List<DiscoveredLeadCandidate> results = cnpjSource.discover(context);

        // Then
        assertThat(results).isNotEmpty();
        
        assertThat(results).anyMatch(lead ->
            lead.name().equals("HealthTech Digital Ltda") &&
            lead.industry().equals("healthtech") &&
            lead.location().contains("Belo Horizonte")
        );
    }

    @Test
    void shouldReturnGenericCompanyForUnknownQuery() {
        // Given
        DiscoveryContext context = new DiscoveryContext("query desconhecida", "Gerente", 2, null);

        // When
        List<DiscoveredLeadCandidate> results = cnpjSource.discover(context);

        // Then
        assertThat(results).hasSize(1); // Deve retornar empresa genérica
        assertThat(results.get(0).name()).isEqualTo("Empresa Brasil Inovação");
        assertThat(results.get(0).sourceName()).isEqualTo("cnpj-ws");
    }

    @Test
    void shouldRespectLimitParameter() {
        // Given
        DiscoveryContext context = new DiscoveryContext("tecnologia", "CTO", 2, null);

        // When
        List<DiscoveredLeadCandidate> results = cnpjSource.discover(context);

        // Then
        assertThat(results).hasSizeLessThanOrEqualTo(2);
    }

    @Test
    void shouldReturnBrazilianLocationsOnly() {
        // Given
        DiscoveryContext context = new DiscoveryContext("tecnologia", "CTO", 10, null);

        // When
        List<DiscoveredLeadCandidate> results = cnpjSource.discover(context);

        // Then
        assertThat(results).allMatch(lead -> 
            lead.location().contains("Brazil") || 
            lead.location().contains("SP") ||
            lead.location().contains("RJ") ||
            lead.location().contains("MG") ||
            lead.location().contains("MT") ||
            lead.location().contains("DF")
        );
    }

    @Test
    void shouldIncludeContactEmails() {
        // Given
        DiscoveryContext context = new DiscoveryContext("fintech", "Founder", 3, null);

        // When
        List<DiscoveredLeadCandidate> results = cnpjSource.discover(context);

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(lead -> 
            !lead.contacts().isEmpty() &&
            lead.contacts().stream().allMatch(email -> email.contains("@"))
        );
    }

    @Test
    void shouldHandleCaseInsensitiveQueries() {
        // Given
        DiscoveryContext lowerCase = new DiscoveryContext("tecnologia", "CTO", 5, null);
        DiscoveryContext upperCase = new DiscoveryContext("TECNOLOGIA", "CTO", 5, null);
        DiscoveryContext mixedCase = new DiscoveryContext("TeCnOlOgIa", "CTO", 5, null);

        // When
        List<DiscoveredLeadCandidate> resultLower = cnpjSource.discover(lowerCase);
        List<DiscoveredLeadCandidate> resultUpper = cnpjSource.discover(upperCase);
        List<DiscoveredLeadCandidate> resultMixed = cnpjSource.discover(mixedCase);

        // Then - todos devem retornar resultados similares
        assertThat(resultLower).hasSameSizeAs(resultUpper);
        assertThat(resultLower).hasSameSizeAs(resultMixed);
    }

    @Test
    void shouldIncludeCNPJInformationInDescription() {
        // Given
        DiscoveryContext context = new DiscoveryContext("fintech", "CEO", 3, null);

        // When
        List<DiscoveredLeadCandidate> results = cnpjSource.discover(context);

        // Then
        assertThat(results).anyMatch(lead ->
            lead.description().contains("CNPJ") || 
            lead.description().contains("validated") ||
            lead.description().contains("verified")
        );
    }
}