package dev.prospectos.infrastructure.service.prospect;

import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
class GooglePageSpeedAuditProvider implements PageSpeedAuditProvider {
    private static final Logger log = LoggerFactory.getLogger(GooglePageSpeedAuditProvider.class);

    private final RestTemplate restTemplate;
    private final PageSpeedProperties properties;

    @Autowired
    GooglePageSpeedAuditProvider(RestTemplateBuilder restTemplateBuilder, PageSpeedProperties properties) {
        this(restTemplateBuilder.setConnectTimeout(properties.normalizedTimeout()).setReadTimeout(properties.normalizedTimeout()).build(),
            properties);
    }

    GooglePageSpeedAuditProvider(RestTemplate restTemplate, PageSpeedProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    @Override
    public PageSpeedAuditResult audit(String website) {
        if (!properties.enabled()) {
            return PageSpeedAuditResult.unavailable();
        }
        validateApiKey();
        try {
            GooglePageSpeedResponse response = restTemplate.getForObject(requestUri(website), GooglePageSpeedResponse.class);
            Integer score = toScore(response);
            return score == null ? PageSpeedAuditResult.unavailable() : new PageSpeedAuditResult(score, findingsFor(score));
        } catch (RestClientException exception) {
            log.warn("PageSpeed audit failed for website '{}': {}", website, exception.getMessage());
            return PageSpeedAuditResult.unavailable();
        }
    }

    private URI requestUri(String website) {
        return UriComponentsBuilder.fromUriString(properties.normalizedBaseUrl())
            .queryParam("url", website)
            .queryParam("strategy", properties.normalizedStrategy())
            .queryParam("locale", properties.normalizedLocale())
            .queryParam("key", properties.apiKey().trim())
            .build(true)
            .toUri();
    }

    private Integer toScore(GooglePageSpeedResponse response) {
        if (response == null || response.lighthouseResult() == null || response.lighthouseResult().categories() == null
            || response.lighthouseResult().categories().performance() == null
            || response.lighthouseResult().categories().performance().score() == null) {
            return null;
        }
        return (int) Math.round(response.lighthouseResult().categories().performance().score() * 100);
    }

    private List<String> findingsFor(int score) {
        if (score < 50) {
            return List.of("PageSpeed indicates poor technical performance on the mobile audit.");
        }
        if (score < 80) {
            return List.of("PageSpeed indicates technical improvement opportunities on the mobile audit.");
        }
        return List.of("PageSpeed indicates strong technical performance on the mobile audit.");
    }

    private void validateApiKey() {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            throw new IllegalStateException("PageSpeed API key is required when pagespeed audit is enabled");
        }
    }
}
