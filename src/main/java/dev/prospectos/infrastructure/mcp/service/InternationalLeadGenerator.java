package dev.prospectos.infrastructure.mcp.service;

import dev.prospectos.api.mcp.LeadData;
import dev.prospectos.api.mcp.LeadSearchCriteria;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;

final class InternationalLeadGenerator {

    private final Random random;
    private final InternationalLeadFixtures fixtures;

    InternationalLeadGenerator(Random random, InternationalLeadFixtures fixtures) {
        this.random = random;
        this.fixtures = fixtures;
    }

    List<LeadData> generate(String country, String industry, LeadSearchCriteria criteria) {
        var count = Math.min(criteria.maxResults(), 20 + random.nextInt(30));
        return IntStream.range(0, count).mapToObj(index -> lead(country, industry, criteria)).toList();
    }

    private LeadData lead(String country, String industry, LeadSearchCriteria criteria) {
        var companyName = fixtures.companyName(country);
        return new LeadData(
            "lead_" + UUID.randomUUID().toString().substring(0, 8),
            companyName,
            "https://www." + companyName.toLowerCase().replace(" ", "") + ".com",
            industry,
            country,
            fixtures.city(country),
            fixtures.additionalData(industry),
            criteria.minQualityScore() + (random.nextDouble() * (1.0 - criteria.minQualityScore()))
        );
    }
}
