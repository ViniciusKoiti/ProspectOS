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
public class TomTomLeadDiscoverySource implements LeadDiscoverySource {

    private static final Logger log = LoggerFactory.getLogger(TomTomLeadDiscoverySource.class);
    private static final String SOURCE_NAME = "tomtom";
    private static final String DEFAULT_COUNTRY_SET = "BR";

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final TomTomResponseMapper responseMapper = new TomTomResponseMapper(SOURCE_NAME);
    private final CnpjMockLeadCatalog mockLeadCatalog = new CnpjMockLeadCatalog(SOURCE_NAME);

    public TomTomLeadDiscoverySource(RestTemplateBuilder restTemplateBuilder) {
        this(restTemplateBuilder, System.getenv("PROSPECTOS_TOMTOM_API_KEY"));
    }

    TomTomLeadDiscoverySource(RestTemplateBuilder restTemplateBuilder, String apiKey) {
        this.restTemplate = restTemplateBuilder.build();
        this.apiKey = apiKey;
    }

    @Override
    public String sourceName() {
        return SOURCE_NAME;
    }

    @Override
    public List<DiscoveredLeadCandidate> discover(DiscoveryContext context) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("TomTom API key not configured, returning mock data");
            return mockLeadCatalog.search(context.query(), context.limit());
        }
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(searchUrl(context.query(), context.limit()), Map.class);
            List<DiscoveredLeadCandidate> candidates = responseMapper.toCandidates(response.getBody(), context.limit());
            if (!candidates.isEmpty()) {
                return candidates;
            }
        } catch (Exception e) {
            log.warn("TomTom discovery failed, returning mock data: {}", e.getMessage());
        }
        return mockLeadCatalog.search(context.query(), context.limit());
    }

    private String searchUrl(String query, int limit) {
        return "https://api.tomtom.com/search/2/poiSearch/"
            + URLEncoder.encode(query, StandardCharsets.UTF_8)
            + ".json?key="
            + URLEncoder.encode(apiKey, StandardCharsets.UTF_8)
            + "&limit="
            + limit
            + "&countrySet="
            + DEFAULT_COUNTRY_SET;
    }
}
