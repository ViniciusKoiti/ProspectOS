package dev.prospectos.infrastructure.service.discovery;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        assertThat(cnpjSource.sourceName()).isEqualTo("cnpj-ws");
    }

    @Test
    void validateCnpjReturnsInvalidWhenResponseStatusIsNot2xx() {
        when(restTemplate.getForEntity("https://cnpj.ws/v1/12345678000190", CnpjWsResponse.class))
            .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).build());

        CnpjValidationResult result = cnpjSource.validateCNPJ("12.345.678/0001-90");

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo("CNPJ validation service unavailable");
    }

    @Test
    void validateCnpjReturnsInvalidWhenBodyIsNull() {
        when(restTemplate.getForEntity("https://cnpj.ws/v1/12345678000190", CnpjWsResponse.class))
            .thenReturn(ResponseEntity.ok().build());

        CnpjValidationResult result = cnpjSource.validateCNPJ("12.345.678/0001-90");

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo("CNPJ validation service unavailable");
    }

    @Test
    void validateCnpjSanitizesCnpjBeforeCallingApi() {
        when(restTemplate.getForEntity("https://cnpj.ws/v1/12345678000190", CnpjWsResponse.class))
            .thenReturn(ResponseEntity.ok(new CnpjWsResponse()));

        cnpjSource.validateCNPJ("12.345.678/0001-90");

        verify(restTemplate).getForEntity("https://cnpj.ws/v1/12345678000190", CnpjWsResponse.class);
    }

    @Test
    void shouldReturnTechCompaniesForTechQuery() {
        DiscoveryContext context = new DiscoveryContext("tecnologia startup", "CTO", 5, null);

        List<DiscoveredLeadCandidate> results = cnpjSource.discover(context);

        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(3);
        assertThat(results).anyMatch(lead ->
            lead.name().equals("TechSolutions Brasil Ltda")
                && lead.industry().equals("technology")
                && lead.location().contains("Sao Paulo")
        );
        assertThat(results).anyMatch(lead ->
            lead.name().equals("InovaPay Fintech SA")
                && lead.industry().equals("fintech")
                && lead.location().contains("Rio de Janeiro")
        );
        assertThat(results).allMatch(lead -> lead.sourceName().equals("cnpj-ws"));
    }

    @Test
    void shouldReturnAgroCompaniesForAgroQuery() {
        DiscoveryContext context = new DiscoveryContext("agronegocio agricultura", "Diretor", 5, null);

        List<DiscoveredLeadCandidate> results = cnpjSource.discover(context);

        assertThat(results).isNotEmpty();
        assertThat(results).anyMatch(lead ->
            lead.name().equals("AgroTech Mato Grosso Ltda")
                && lead.industry().equals("agtech")
                && lead.location().contains("Cuiaba")
        );
        assertThat(results).anyMatch(lead ->
            lead.name().equals("FarmData Analytics")
                && lead.industry().equals("agtech")
        );
    }

    @Test
    void shouldReturnConsultingCompaniesForBusinessQuery() {
        DiscoveryContext context = new DiscoveryContext("consultoria empresas", "Consultor", 5, null);

        List<DiscoveredLeadCandidate> results = cnpjSource.discover(context);

        assertThat(results).isNotEmpty();
        assertThat(results).anyMatch(lead ->
            lead.name().equals("Consultoria Estrategica Brasil")
                && lead.industry().equals("consulting")
                && lead.location().contains("Brasilia")
        );
    }

    @Test
    void shouldReturnHealthCompaniesForHealthQuery() {
        DiscoveryContext context = new DiscoveryContext("saude telemedicina", "CEO", 3, null);

        List<DiscoveredLeadCandidate> results = cnpjSource.discover(context);

        assertThat(results).isNotEmpty();
        assertThat(results).anyMatch(lead ->
            lead.name().equals("HealthTech Digital Ltda")
                && lead.industry().equals("healthtech")
                && lead.location().contains("Belo Horizonte")
        );
    }

    @Test
    void shouldReturnGenericCompanyForUnknownQuery() {
        DiscoveryContext context = new DiscoveryContext("query desconhecida", "Gerente", 2, null);

        List<DiscoveredLeadCandidate> results = cnpjSource.discover(context);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().name()).isEqualTo("Empresa Brasil Inovacao");
        assertThat(results.getFirst().sourceName()).isEqualTo("cnpj-ws");
    }

    @Test
    void shouldRespectLimitParameter() {
        DiscoveryContext context = new DiscoveryContext("tecnologia", "CTO", 2, null);

        List<DiscoveredLeadCandidate> results = cnpjSource.discover(context);

        assertThat(results).hasSizeLessThanOrEqualTo(2);
    }

    @Test
    void shouldReturnBrazilianLocationsOnly() {
        DiscoveryContext context = new DiscoveryContext("tecnologia", "CTO", 10, null);

        List<DiscoveredLeadCandidate> results = cnpjSource.discover(context);

        assertThat(results).allMatch(lead ->
            lead.location().contains("Brazil")
                || lead.location().contains("SP")
                || lead.location().contains("RJ")
                || lead.location().contains("MG")
                || lead.location().contains("MT")
                || lead.location().contains("DF")
        );
    }

    @Test
    void shouldIncludeContactEmails() {
        DiscoveryContext context = new DiscoveryContext("fintech", "Founder", 3, null);

        List<DiscoveredLeadCandidate> results = cnpjSource.discover(context);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(lead ->
            !lead.contacts().isEmpty() && lead.contacts().stream().allMatch(email -> email.contains("@"))
        );
    }

    @Test
    void shouldHandleCaseInsensitiveQueries() {
        DiscoveryContext lowerCase = new DiscoveryContext("tecnologia", "CTO", 5, null);
        DiscoveryContext upperCase = new DiscoveryContext("TECNOLOGIA", "CTO", 5, null);
        DiscoveryContext mixedCase = new DiscoveryContext("TeCnOlOgIa", "CTO", 5, null);

        List<DiscoveredLeadCandidate> resultLower = cnpjSource.discover(lowerCase);
        List<DiscoveredLeadCandidate> resultUpper = cnpjSource.discover(upperCase);
        List<DiscoveredLeadCandidate> resultMixed = cnpjSource.discover(mixedCase);

        assertThat(resultLower).hasSameSizeAs(resultUpper);
        assertThat(resultLower).hasSameSizeAs(resultMixed);
    }

    @Test
    void shouldIncludeCnpjInformationInDescription() {
        DiscoveryContext context = new DiscoveryContext("fintech", "CEO", 3, null);

        List<DiscoveredLeadCandidate> results = cnpjSource.discover(context);

        assertThat(results).anyMatch(lead ->
            lead.description().contains("CNPJ")
                || lead.description().contains("validated")
                || lead.description().contains("verified")
        );
    }
}
