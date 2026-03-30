package dev.prospectos.api.mcp;

import java.util.List;

/**
 * AI-powered international lead discovery and optimization service.
 */
public interface InternationalSearchService {

    InternationalSearchResult searchLeads(String country, String industry, LeadSearchCriteria criteria);

    EnrichedLeadData enrichLead(LeadData leadData, List<String> sources);

    SearchStrategy optimizeStrategy(String market, double budget, double qualityThreshold);

    MarketCoverageAnalysis analyzeMarketCoverage(String country, List<String> competitors);
}
