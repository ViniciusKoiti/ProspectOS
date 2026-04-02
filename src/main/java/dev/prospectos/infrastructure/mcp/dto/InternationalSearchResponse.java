package dev.prospectos.infrastructure.mcp.dto;

import dev.prospectos.api.mcp.LeadData;
import dev.prospectos.api.mcp.SearchQualityMetrics;

import java.util.List;
import java.util.Map;

public record InternationalSearchResponse(
    String country,
    String industry,
    List<LeadResponse> leads,
    SearchQualityResponse quality,
    double totalCost,
    List<String> sourcesUsed
) {

    public static InternationalSearchResponse fromDomain(dev.prospectos.api.mcp.InternationalSearchResult result) {
        var leads = result.leads().stream()
            .map(LeadResponse::fromDomain)
            .toList();

        var quality = SearchQualityResponse.fromDomain(result.quality());

        return new InternationalSearchResponse(
            result.country(),
            result.industry(),
            leads,
            quality,
            result.totalCost(),
            result.sourcesUsed()
        );
    }

    public record LeadResponse(
        String id,
        String companyName,
        String website,
        String industry,
        String country,
        String city,
        Map<String, Object> additionalData,
        double qualityScore
    ) {
        public static LeadResponse fromDomain(LeadData lead) {
            return new LeadResponse(
                lead.id(),
                lead.companyName(),
                lead.website(),
                lead.industry(),
                lead.country(),
                lead.city(),
                lead.additionalData(),
                lead.qualityScore()
            );
        }
    }

    public record SearchQualityResponse(
        double overallScore,
        double dataCompleteness,
        double accuracy,
        double freshness,
        int duplicatesFound,
        List<String> qualityIssues
    ) {
        public static SearchQualityResponse fromDomain(SearchQualityMetrics quality) {
            return new SearchQualityResponse(
                quality.overallScore(),
                quality.dataCompleteness(),
                quality.accuracy(),
                quality.freshness(),
                quality.duplicatesFound(),
                quality.qualityIssues()
            );
        }
    }
}


