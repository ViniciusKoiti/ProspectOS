package dev.prospectos.infrastructure.mcp.service;

import dev.prospectos.api.mcp.LeadData;
import dev.prospectos.api.mcp.LeadSearchCriteria;
import dev.prospectos.api.mcp.SearchQualityMetrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

final class InternationalSearchAssessmentFactory {

    private static final List<String> SOURCES = List.of("nominatim", "bing-maps", "google-places", "web-scraping", "linkedin-api", "crunchbase");
    private final Random random;

    InternationalSearchAssessmentFactory(Random random) {
        this.random = random;
    }

    SearchQualityMetrics quality(List<LeadData> leads) {
        return new SearchQualityMetrics(0.7 + (random.nextDouble() * 0.25), 0.8 + (random.nextDouble() * 0.15), 0.85 + (random.nextDouble() * 0.1), 0.75 + (random.nextDouble() * 0.2), random.nextInt(5), qualityIssues());
    }

    double cost(List<LeadData> leads, LeadSearchCriteria criteria) {
        return leads.size() * (0.05 + (random.nextDouble() * 0.1));
    }

    List<String> sources(LeadSearchCriteria criteria) {
        var available = new ArrayList<>(SOURCES);
        Collections.shuffle(available, random);
        return available.subList(0, 2 + random.nextInt(3));
    }

    private List<String> qualityIssues() {
        var issues = List.of("Some websites may be outdated", "Contact information needs verification", "Industry classification might vary", "Company size estimates are approximate");
        return issues.subList(0, random.nextInt(3));
    }
}
