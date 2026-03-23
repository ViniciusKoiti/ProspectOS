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
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class AmazonLocationLeadDiscoverySource implements LeadDiscoverySource {
    private static final Logger log = LoggerFactory.getLogger(AmazonLocationLeadDiscoverySource.class);
    private static final String SOURCE_NAME = "amazon-location";

    private final RestTemplate restTemplate;
    private final AmazonLocationProperties properties;
    private final AmazonLocationResponseMapper responseMapper;

    @Autowired
    public AmazonLocationLeadDiscoverySource(RestTemplateBuilder restTemplateBuilder, AmazonLocationProperties properties) {
        this(restTemplateBuilder.build(), properties, new AmazonLocationResponseMapper(SOURCE_NAME));
    }

    AmazonLocationLeadDiscoverySource(RestTemplate restTemplate, AmazonLocationProperties properties,
                                      AmazonLocationResponseMapper responseMapper) {
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
            log.debug("Amazon Location source is disabled, skipping discovery");
            return List.of();
        }
        validateApiKey();
        try {
            ResponseEntity<AmazonLocationSearchTextResponse> response = restTemplate.postForEntity(
                buildSearchUri(),
                buildRequestEntity(context),
                AmazonLocationSearchTextResponse.class
            );
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return List.of();
            }
            return responseMapper.toCandidates(response.getBody().resultItems(), context.limit());
        } catch (RestClientException exception) {
            log.warn("Amazon Location SearchText failed for query '{}': {}", context.query(), exception.getMessage());
            return List.of();
        }
    }

    private HttpEntity<AmazonLocationSearchTextRequest> buildRequestEntity(DiscoveryContext context) {
        AmazonLocationSearchTextRequest request = new AmazonLocationSearchTextRequest(
            context.query(),
            properties.normalizedLanguage(),
            properties.normalizedIntendedUse(),
            properties.resolveMaxResults(context.limit()),
            buildFilter()
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(request, headers);
    }

    private AmazonLocationSearchTextFilter buildFilter() {
        List<String> includeCountries = properties.normalizedIncludeCountries();
        return includeCountries.isEmpty() ? null : new AmazonLocationSearchTextFilter(includeCountries);
    }

    private URI buildSearchUri() {
        String endpoint = "https://places.geo." + properties.normalizedRegion() + ".amazonaws.com/v2/search-text";
        return UriComponentsBuilder.fromHttpUrl(endpoint)
            .queryParam("key", properties.apiKey().trim())
            .build(true)
            .toUri();
    }

    private void validateApiKey() {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            throw new IllegalStateException("Amazon Location API key is required when amazon-location source is enabled");
        }
    }
}
