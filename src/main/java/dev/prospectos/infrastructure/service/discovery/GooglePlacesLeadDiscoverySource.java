package dev.prospectos.infrastructure.service.discovery;

import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class GooglePlacesLeadDiscoverySource implements LeadDiscoverySource {
    private static final Logger log = LoggerFactory.getLogger(GooglePlacesLeadDiscoverySource.class);
    private static final String SOURCE_NAME = "google-places";
    private static final URI SEARCH_URI = URI.create("https://places.googleapis.com/v1/places:searchText");

    private final RestTemplate restTemplate;
    private final GooglePlacesProperties properties;
    private final GooglePlacesResponseMapper responseMapper;

    @Autowired
    public GooglePlacesLeadDiscoverySource(RestTemplateBuilder restTemplateBuilder, GooglePlacesProperties properties) {
        this(restTemplateBuilder.build(), properties, new GooglePlacesResponseMapper(SOURCE_NAME));
    }

    GooglePlacesLeadDiscoverySource(RestTemplate restTemplate, GooglePlacesProperties properties, GooglePlacesResponseMapper responseMapper) {
        this.restTemplate = restTemplate;
        this.properties = properties;
        this.responseMapper = responseMapper;
    }

    @Override
    public String sourceName() {
        return SOURCE_NAME;
    }

    @Override
    public List<DiscoveredLeadCandidate> discover(DiscoveryContext context) {
        if (!properties.enabled()) {
            log.debug("Google Places source is disabled, skipping discovery");
            return List.of();
        }
        validateApiKey();
        try {
            ResponseEntity<GooglePlacesSearchTextResponse> response = restTemplate.postForEntity(
                SEARCH_URI,
                requestEntity(context),
                GooglePlacesSearchTextResponse.class
            );
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return List.of();
            }
            return responseMapper.toCandidates(response.getBody().places(), context.limit());
        } catch (RestClientException exception) {
            log.warn("Google Places search failed for query '{}': {}", context.query(), exception.getMessage());
            return List.of();
        }
    }

    private HttpEntity<GooglePlacesSearchTextRequest> requestEntity(DiscoveryContext context) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Goog-Api-Key", properties.apiKey().trim());
        headers.set("X-Goog-FieldMask", properties.normalizedFieldMask());
        return new HttpEntity<>(
            new GooglePlacesSearchTextRequest(context.query(), properties.normalizedLanguageCode(), properties.resolveMaxResults(context.limit())),
            headers
        );
    }

    private void validateApiKey() {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            throw new IllegalStateException("Google Places API key is required when google-places source is enabled");
        }
    }
}
