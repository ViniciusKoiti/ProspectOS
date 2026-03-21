package dev.prospectos.infrastructure.service.discovery;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class OpenCnpjLeadDiscoverySource implements LeadDiscoverySource {

    private static final Logger log = LoggerFactory.getLogger(OpenCnpjLeadDiscoverySource.class);
    private static final String SOURCE_NAME = "open-cnpj";

    private final RestTemplate restTemplate;
    private final OpenCnpjResponseMapper responseMapper = new OpenCnpjResponseMapper(SOURCE_NAME);
    private final CnpjMockLeadCatalog mockLeadCatalog = new CnpjMockLeadCatalog(SOURCE_NAME);

    public OpenCnpjLeadDiscoverySource(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    public String sourceName() {
        return SOURCE_NAME;
    }

    @Override
    public List<DiscoveredLeadCandidate> discover(DiscoveryContext context) {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(searchUrl(context.query(), context.limit()), Map.class);
            List<DiscoveredLeadCandidate> candidates = responseMapper.toCandidates(response.getBody(), context.limit());
            if (!candidates.isEmpty()) {
                return candidates;
            }
        } catch (Exception e) {
            log.warn("OpenCNPJ search failed, returning mock data: {}", e.getMessage());
        }
        return mockLeadCatalog.search(context.query(), context.limit());
    }

    private String searchUrl(String query, int limit) {
        return "https://opencnpj.com/api/companies?query="
            + URLEncoder.encode(query, StandardCharsets.UTF_8)
            + "&limit="
            + limit;
    }
}
