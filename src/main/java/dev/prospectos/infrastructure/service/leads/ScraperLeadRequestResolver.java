package dev.prospectos.infrastructure.service.leads;

import java.util.List;

import dev.prospectos.api.dto.LeadSearchRequest;
import dev.prospectos.infrastructure.config.LeadSearchProperties;
import dev.prospectos.infrastructure.service.compliance.AllowedSourcesComplianceService;

final class ScraperLeadRequestResolver {

    private static final int DEFAULT_LIMIT = 10;

    private final AllowedSourcesComplianceService complianceService;
    private final LeadSearchProperties properties;

    ScraperLeadRequestResolver(
        AllowedSourcesComplianceService complianceService,
        LeadSearchProperties properties
    ) {
        this.complianceService = complianceService;
        this.properties = properties;
    }

    ScraperLeadRequestContext resolve(LeadSearchRequest request) {
        if (request == null || request.query() == null || request.query().isBlank()) {
            throw new IllegalArgumentException("Query cannot be null or blank");
        }
        int limit = request.limit() == null ? DEFAULT_LIMIT : request.limit();
        List<String> requestedSources = complianceService.validateSources(request.sources());
        if (requestedSources == null || requestedSources.isEmpty()) {
            throw new IllegalArgumentException(
                "No lead sources configured. Configure prospectos.leads.default-sources or provide sources in request"
            );
        }
        String query = request.query().trim();
        String scraperQuery = normalizeWebsiteOrQuery(query);
        return new ScraperLeadRequestContext(limit, requestedSources, query, scraperQuery);
    }

    Long resolveIcpId(Long requestIcpId) {
        if (requestIcpId != null) {
            return requestIcpId;
        }
        if (properties.defaultIcpId() == null) {
            throw new IllegalArgumentException(
                "ICP ID is required. Provide icpId in request or configure prospectos.leads.default-icp-id"
            );
        }
        return properties.defaultIcpId();
    }

    private String normalizeWebsiteOrQuery(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        if (trimmed.contains(" ")) {
            return trimmed;
        }
        return "https://" + trimmed;
    }
}
