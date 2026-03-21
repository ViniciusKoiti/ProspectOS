package dev.prospectos.infrastructure.service.discovery;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CNPJLeadDiscoverySource implements LeadDiscoverySource {

    private static final Logger log = LoggerFactory.getLogger(CNPJLeadDiscoverySource.class);
    private static final String SOURCE_NAME = "cnpj-ws";

    private final RestTemplate restTemplate;
    private final CnpjMockLeadCatalog mockLeadCatalog = new CnpjMockLeadCatalog(SOURCE_NAME);

    public CNPJLeadDiscoverySource(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    public String sourceName() {
        return SOURCE_NAME;
    }

    @Override
    public List<DiscoveredLeadCandidate> discover(DiscoveryContext context) {
        try {
            return searchCompaniesByQuery(context.query(), context.limit());
        } catch (Exception e) {
            log.warn("CNPJ.ws search failed, returning mock data: {}", e.getMessage());
            return mockLeadCatalog.search(context.query(), context.limit());
        }
    }

    public CnpjValidationResult validateCNPJ(String cnpj) {
        try {
            String cleanCnpj = cnpj.replaceAll("[^0-9]", "");
            String url = "https://cnpj.ws/v1/" + cleanCnpj;
            log.info("Validating CNPJ {} via CNPJ.ws API", cleanCnpj);
            ResponseEntity<CnpjWsResponse> response = restTemplate.getForEntity(url, CnpjWsResponse.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return CnpjValidationResult.invalid("CNPJ validation service unavailable");
            }
            return mapToCnpjValidation(response.getBody());
        } catch (Exception e) {
            log.warn("CNPJ validation failed for {}: {}", cnpj, e.getMessage());
        }
        return CnpjValidationResult.invalid("CNPJ validation service unavailable");
    }

    private List<DiscoveredLeadCandidate> searchCompaniesByQuery(String query, int limit) {
        return mockLeadCatalog.search(query, limit);
    }

    private CnpjValidationResult mapToCnpjValidation(CnpjWsResponse response) {
        if (response.status == 200 && response.situacao != null && response.situacao.equalsIgnoreCase("ATIVA")) {
            return CnpjValidationResult.valid(
                response.nome,
                response.fantasia,
                response.email,
                formatAddress(response)
            );
        }
        return CnpjValidationResult.invalid("Company not active or not found");
    }

    private String formatAddress(CnpjWsResponse response) {
        return response.logradouro + ", " + response.municipio + " - " + response.uf;
    }
}
