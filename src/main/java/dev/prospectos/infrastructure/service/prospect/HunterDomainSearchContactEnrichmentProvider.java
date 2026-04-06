package dev.prospectos.infrastructure.service.prospect;

import java.net.URI;
import java.util.List;

import dev.prospectos.api.dto.ProspectContactResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
class HunterDomainSearchContactEnrichmentProvider implements HunterContactEnrichmentProvider {
    private static final Logger log = LoggerFactory.getLogger(HunterDomainSearchContactEnrichmentProvider.class);

    private final RestTemplate restTemplate;
    private final HunterProperties properties;
    private final WebsiteDomainExtractor domainExtractor;
    private final HunterResponseMapper responseMapper;

    @Autowired
    HunterDomainSearchContactEnrichmentProvider(RestTemplateBuilder restTemplateBuilder, HunterProperties properties,
                                                WebsiteDomainExtractor domainExtractor, HunterResponseMapper responseMapper) {
        this(
            restTemplateBuilder.setConnectTimeout(properties.normalizedTimeout()).setReadTimeout(properties.normalizedTimeout()).build(),
            properties,
            domainExtractor,
            responseMapper
        );
    }

    HunterDomainSearchContactEnrichmentProvider(RestTemplate restTemplate, HunterProperties properties,
                                                WebsiteDomainExtractor domainExtractor, HunterResponseMapper responseMapper) {
        this.restTemplate = restTemplate;
        this.properties = properties;
        this.domainExtractor = domainExtractor;
        this.responseMapper = responseMapper;
    }

    @Override
    public List<ProspectContactResponse> findContacts(String website) {
        if (!properties.enabled()) {
            return List.of();
        }
        validateApiKey();
        String domain = domainExtractor.extract(website);
        if (domain == null) {
            return List.of();
        }
        try {
            HunterDomainSearchResponse response = restTemplate.getForObject(requestUri(domain), HunterDomainSearchResponse.class);
            return responseMapper.toContacts(response, properties.normalizedMaxResults());
        } catch (RestClientException exception) {
            log.warn("Hunter domain search failed for website '{}': {}", website, exception.getMessage());
            return List.of();
        }
    }

    private URI requestUri(String domain) {
        return UriComponentsBuilder.fromUriString(properties.normalizedBaseUrl())
            .queryParam("domain", domain)
            .queryParam("api_key", properties.apiKey().trim())
            .queryParam("limit", properties.normalizedMaxResults())
            .build(true)
            .toUri();
    }

    private void validateApiKey() {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            throw new IllegalStateException("Hunter API key is required when hunter contact enrichment is enabled");
        }
    }
}
