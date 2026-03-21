package dev.prospectos.infrastructure.service.discovery;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TomTomLeadDiscoverySource implements LeadDiscoverySource {

    private static final Logger log = LoggerFactory.getLogger(TomTomLeadDiscoverySource.class);
    private static final String SOURCE_NAME = "tomtom";

    private final RestTemplate restTemplate;
    private final CnpjMockLeadCatalog mockLeadCatalog = new CnpjMockLeadCatalog(SOURCE_NAME);

    public TomTomLeadDiscoverySource(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    public String sourceName() {
        return SOURCE_NAME;
    }

    @Override
    public List<DiscoveredLeadCandidate> discover(DiscoveryContext context) {
        try {
            restTemplate.getForEntity(searchUrl(context.query()), String.class);
            return mockLeadCatalog.search(context.query(), context.limit());
        } catch (Exception e) {
            log.warn("TomTom discovery failed, returning mock data: {}", e.getMessage());
            return mockLeadCatalog.search(context.query(), context.limit());
        }
    }

    private String searchUrl(String query) {
        return "https://api.tomtom.com/search?query=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
    }
}
