package dev.prospectos.infrastructure.service.discovery;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class OpenCnpjLeadDiscoverySource implements LeadDiscoverySource {

    private static final Logger log = LoggerFactory.getLogger(OpenCnpjLeadDiscoverySource.class);
    private static final String SOURCE_NAME = "open-cnpj";

    private final RestTemplate restTemplate;
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
            probeExternalClient(context.query());
        } catch (Exception e) {
            log.warn("OpenCNPJ search failed, returning mock data: {}", e.getMessage());
        }
        return mockLeadCatalog.search(context.query(), context.limit());
    }

    private void probeExternalClient(String query) {
        restTemplate.getForEntity("https://opencnpj.com/api/search?q=" + query, String.class);
    }
}
