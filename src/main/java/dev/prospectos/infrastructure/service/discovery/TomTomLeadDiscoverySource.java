package dev.prospectos.infrastructure.service.discovery;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class TomTomLeadDiscoverySource implements LeadDiscoverySource {

    private static final Logger log = LoggerFactory.getLogger(TomTomLeadDiscoverySource.class);
    private static final String SOURCE_NAME = "tomtom";

    private final RestTemplate restTemplate;
    private final TomTomProperties properties;
    private final TomTomDiscoveryMapper mapper = new TomTomDiscoveryMapper();
    private final CnpjMockLeadCatalog mockLeadCatalog = new CnpjMockLeadCatalog(SOURCE_NAME);

    public TomTomLeadDiscoverySource(RestTemplateBuilder restTemplateBuilder, TomTomProperties properties) {
        this.restTemplate = restTemplateBuilder.build();
        this.properties = properties;
    }

    @Override
    public String sourceName() {
        return SOURCE_NAME;
    }

    @Override
    public List<DiscoveredLeadCandidate> discover(DiscoveryContext context) {
        if (!hasApiKey()) {
            log.info("TomTom API key not configured. Returning mock discovery data.");
            return mockLeadCatalog.search(context.query(), context.limit());
        }

        try {
            int requestLimit = resolveRequestLimit(context.limit());
            String url = searchUrl(context, requestLimit);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return List.of();
            }
            return mapper.map(response.getBody(), requestLimit, SOURCE_NAME);
        } catch (Exception e) {
            log.warn("TomTom discovery failed, returning mock data: {}", e.getMessage());
            return mockLeadCatalog.search(context.query(), context.limit());
        }
    }

    private boolean hasApiKey() {
        return properties.apiKey() != null && !properties.apiKey().isBlank();
    }

    private int resolveRequestLimit(int requestedLimit) {
        int normalizedRequested = Math.max(1, requestedLimit);
        int maxByConfig = properties.maxResultsPerRequest() > 0 ? properties.maxResultsPerRequest() : normalizedRequested;
        return Math.min(normalizedRequested, maxByConfig);
    }

    private String searchUrl(DiscoveryContext context, int limit) {
        UriComponentsBuilder builder = UriComponentsBuilder
            .fromHttpUrl(normalizeBaseUrl(properties.baseUrl()))
            .path("/search/2/poiSearch/{query}.json")
            .queryParam("key", properties.apiKey().trim())
            .queryParam("limit", limit)
            .queryParam("language", normalizeLanguage(properties.language()));

        String countrySet = resolveCountrySet(context);
        if (countrySet != null) {
            builder.queryParam("countrySet", countrySet);
        }

        if (properties.latitude() != null && properties.longitude() != null) {
            builder.queryParam("lat", properties.latitude());
            builder.queryParam("lon", properties.longitude());
            if (properties.radiusMeters() > 0) {
                builder.queryParam("radius", properties.radiusMeters());
            }
        }

        return builder.buildAndExpand(context.query().trim()).encode().toUriString();
    }

    private String resolveCountrySet(DiscoveryContext context) {
        Set<String> countries = new LinkedHashSet<>();
        addConfiguredCountrySet(countries, properties.countrySet());
        if (countries.isEmpty() && context.icp() != null) {
            addConfiguredCountrySet(countries, context.icp().regions());
        }
        return countries.isEmpty() ? null : String.join(",", countries);
    }

    private void addConfiguredCountrySet(Set<String> target, List<String> values) {
        if (values == null) {
            return;
        }
        for (String value : values) {
            String mapped = mapCountryCode(value);
            if (mapped != null) {
                target.add(mapped);
            }
        }
    }

    private String mapCountryCode(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();
        if (normalized.matches("^[A-Za-z]{2}$")) {
            return normalized.toUpperCase();
        }

        String lower = normalized.toLowerCase();
        if (lower.contains("brasil") || lower.contains("brazil")) {
            return "BR";
        }
        if (lower.contains("united states") || lower.contains("usa") || lower.contains("eua")) {
            return "US";
        }
        if (lower.contains("canada")) {
            return "CA";
        }
        if (lower.contains("argentina")) {
            return "AR";
        }
        if (lower.contains("mexico")) {
            return "MX";
        }
        return null;
    }

    private String normalizeLanguage(String language) {
        return language == null || language.isBlank() ? "pt-BR" : language.trim();
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "https://api.tomtom.com";
        }
        String normalized = baseUrl.trim();
        return normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
    }
}
