package dev.prospectos.integration;

import dev.prospectos.infrastructure.service.discovery.CNPJLeadDiscoverySource;
import dev.prospectos.infrastructure.service.discovery.CnpjValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;

@ExtendWith(MockitoExtension.class)
class CNPJLeadDiscoveryValidationIntegrationTest {

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private CNPJLeadDiscoverySource source;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        given(restTemplateBuilder.build()).willReturn(restTemplate);
        source = new CNPJLeadDiscoverySource(restTemplateBuilder);

        RestTemplate sourceTemplate = (RestTemplate) ReflectionTestUtils.getField(source, "restTemplate");
        assertThat(sourceTemplate).isSameAs(restTemplate);
    }

    @Test
    void validateCnpjReturnsMappedCompanyWhenApiReturnsActiveCompany() {
        server.expect(requestTo("https://cnpj.ws/v1/12345678000190"))
            .andRespond(withSuccess("""
                {
                  "status": 200,
                  "nome": "Acme Tecnologia Ltda",
                  "fantasia": "Acme Tech",
                  "email": "contato@acme.com.br",
                  "situacao": "ATIVA",
                  "logradouro": "Rua das Flores",
                  "municipio": "Sao Paulo",
                  "uf": "SP"
                }
                """, MediaType.APPLICATION_JSON));

        CnpjValidationResult result = source.validateCNPJ("12.345.678/0001-90");

        assertThat(result.isValid()).isTrue();
        assertThat(result.getCompanyName()).isEqualTo("Acme Tecnologia Ltda");
        assertThat(result.getFantasyName()).isEqualTo("Acme Tech");
        assertThat(result.getEmail()).isEqualTo("contato@acme.com.br");
        assertThat(result.getAddress()).isEqualTo("Rua das Flores, Sao Paulo - SP");
        server.verify();
    }

    @Test
    void validateCnpjReturnsInvalidWhenApiFails() {
        server.expect(requestTo("https://cnpj.ws/v1/12345678000190"))
            .andRespond(withServerError());

        CnpjValidationResult result = source.validateCNPJ("12.345.678/0001-90");

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo("CNPJ validation service unavailable");
        server.verify();
    }
}
