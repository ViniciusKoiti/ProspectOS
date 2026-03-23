package dev.prospectos.infrastructure.service.discovery;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class OpenCnpjLeadDiscoverySourceTest {

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    private RestTemplate restTemplate;
    private MockRestServiceServer server;

    private OpenCnpjLeadDiscoverySource source;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        given(restTemplateBuilder.build()).willReturn(restTemplate);
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
        server.expect(requestTo("https://kitana.opencnpj.com/cnpj/12345678000195"))
            .andRespond(withServerError());

        DiscoveryContext context = new DiscoveryContext("12.345.678/0001-95", "Diretor", 3, null);

        List<DiscoveredLeadCandidate> results = source.discover(context);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(candidate -> candidate.sourceName().equals("open-cnpj"));
        server.verify();
    }

    @Test
    void discover_shouldMapLocationFromOpenCnpjWhenQueryContainsCnpj() {
        server.expect(requestTo("https://kitana.opencnpj.com/cnpj/12345678000195"))
            .andRespond(withSuccess(
                """
                    {
                      \"success\": true,
                      \"message\": null,
                      \"data\": {
                        \"cnpj\": \"12.345.678/0001-95\",
                        \"situacaoCadastral\": \"Ativa\",
                        \"razaoSocial\": \"ACME TECNOLOGIA LTDA\",
                        \"nomeFantasia\": \"Acme Tech\",
                        \"email\": \"contato@acme.com.br\",
                        \"logradouro\": \"Rua das Flores\",
                        \"numero\": \"100\",
                        \"bairro\": \"Centro\",
                        \"municipio\": \"Sao Paulo\",
                        \"uf\": \"SP\",
                        \"cnaes\": [
                          {
                            \"cnae\": \"6201501\",
                            \"descricao\": \"Desenvolvimento de software sob encomenda\"
                          }
                        ]
                      }
                    }
                    """,
                MediaType.APPLICATION_JSON
            ));

        DiscoveryContext context = new DiscoveryContext("empresa 12.345.678/0001-95", "CTO", 5, null);

        List<DiscoveredLeadCandidate> results = source.discover(context);

        assertThat(results).hasSize(1);
        DiscoveredLeadCandidate candidate = results.getFirst();
        assertThat(candidate.sourceName()).isEqualTo("open-cnpj");
        assertThat(candidate.name()).isEqualTo("Acme Tech");
        assertThat(candidate.location()).isEqualTo("Sao Paulo, SP - Brazil");
        assertThat(candidate.contacts()).contains("contato@acme.com.br");
        assertThat(candidate.website()).isNull();
        assertThat(candidate.websitePresence()).isEqualTo(dev.prospectos.api.dto.CompanyCandidateDTO.WebsitePresence.NO_WEBSITE);
        assertThat(candidate.industry().toLowerCase(Locale.ROOT)).contains("software");
        server.verify();
    }
}
