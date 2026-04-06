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
public class ApifyLeadDiscoverySource implements LeadDiscoverySource {
    private static final Logger log = LoggerFactory.getLogger(ApifyLeadDiscoverySource.class);
    private static final String SOURCE_NAME = "apify";

    private final RestTemplate restTemplate;
    private final ApifyProperties properties;
    private final ApifyResponseMapper responseMapper;

    @Autowired
    public ApifyLeadDiscoverySource(RestTemplateBuilder restTemplateBuilder, ApifyProperties properties) {
        this(
            restTemplateBuilder
                .connectTimeout(properties.normalizedTimeout())
                .readTimeout(properties.normalizedTimeout())
                .build(),
            properties,
            new ApifyResponseMapper(SOURCE_NAME)
        );
    }

    ApifyLeadDiscoverySource(RestTemplate restTemplate, ApifyProperties properties, ApifyResponseMapper responseMapper) {
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
            log.debug("Apify source is disabled, skipping discovery");
            return List.of();
        }
        validateToken();
        try {
            ResponseEntity<ApifyDatasetItem[]> response = restTemplate.postForEntity(
                buildRunUri(),
                requestEntity(context),
                ApifyDatasetItem[].class
            );
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return List.of();
            }
            return responseMapper.toCandidates(response.getBody(), context.limit());
        } catch (RestClientException exception) {
            log.warn("Apify discovery failed for query '{}': {}", context.query(), exception.getMessage());
            return List.of();
        }
    }

    private HttpEntity<ApifyRunInput> requestEntity(DiscoveryContext context) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(properties.apiToken().trim());
        return new HttpEntity<>(ApifyRunInput.from(context, properties.useApifyProxy()), headers);
    }

    private URI buildRunUri() {
        String base = properties.normalizedBaseUrl();
        String actorId = properties.normalizedActorId();
        return URI.create(base + "/v2/acts/" + actorId + "/run-sync-get-dataset-items");
    }

    private void validateToken() {
        if (properties.apiToken() == null || properties.apiToken().isBlank()) {
            throw new IllegalStateException("Apify API token is required when apify source is enabled");
        }
    }
}
