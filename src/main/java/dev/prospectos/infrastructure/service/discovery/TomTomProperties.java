package dev.prospectos.infrastructure.service.discovery;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuration for TomTom Search API integration.
 */
@ConfigurationProperties(prefix = "prospectos.integrations.tomtom")
public record TomTomProperties(
    @DefaultValue("") String apiKey,
    @DefaultValue("https://api.tomtom.com") String baseUrl,
    @DefaultValue("pt-BR") String language,
    @DefaultValue("20") int maxResultsPerRequest,
    @DefaultValue("10000") int radiusMeters,
    Double latitude,
    Double longitude,
    @DefaultValue List<String> countrySet
) {
}
