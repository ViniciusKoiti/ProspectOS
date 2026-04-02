package dev.prospectos.infrastructure.mcp.tools;

import dev.prospectos.api.mcp.InternationalSearchService;
import dev.prospectos.infrastructure.mcp.config.ConditionalOnMcpMockRuntime;
import dev.prospectos.infrastructure.mcp.dto.EnrichedLeadResponse;
import dev.prospectos.infrastructure.mcp.dto.InternationalSearchResponse;
import dev.prospectos.infrastructure.mcp.dto.MarketCoverageResponse;
import dev.prospectos.infrastructure.mcp.dto.SearchStrategyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnMcpMockRuntime
@RequiredArgsConstructor
public class InternationalSearchMcpTools {

    private final InternationalSearchService internationalSearchService;
    private final InternationalSearchMcpInputParser inputParser;

    @McpTool(name = "search_international_leads", description = "Execute AI-optimized international business lead search with quality assessment. Discovers companies matching criteria in target markets with cost and quality optimization.")
    public InternationalSearchResponse searchInternationalLeads(
        @McpToolParam(description = "Target country for lead search", required = true) String country,
        @McpToolParam(description = "Industry sector to target", required = true) String industry,
        @McpToolParam(description = "Maximum number of leads to return (1-100, default: 20)", required = false) String maxResults,
        @McpToolParam(description = "Budget limit in USD (default: 50.0)", required = false) String budgetLimit,
        @McpToolParam(description = "Minimum quality score (0.0-1.0, default: 0.7)", required = false) String minQualityScore,
        @McpToolParam(description = "Required lead fields comma-separated", required = false) String requiredFields
    ) {
        var normalizedCountry = inputParser.normalizeRequired(country, "Country");
        var normalizedIndustry = inputParser.normalizeRequired(industry, "Industry");
        var criteria = inputParser.buildSearchCriteria(maxResults, budgetLimit, minQualityScore, requiredFields);
        log.debug("MCP search_international_leads called: country={} industry={} maxResults={} budget={}", normalizedCountry, normalizedIndustry, criteria.maxResults(), criteria.budgetLimit());
        var response = InternationalSearchResponse.fromDomain(internationalSearchService.searchLeads(normalizedCountry, normalizedIndustry, criteria));
        log.info("International lead search completed via MCP: country={} industry={} leads={} cost=${} quality={}", normalizedCountry, normalizedIndustry, response.leads().size(), response.totalCost(), response.quality().overallScore());
        return response;
    }

    @McpTool(name = "enrich_international_lead", description = "Enhance lead data with additional intelligence including company details, contacts, and technology stack using multiple data sources for comprehensive business intelligence.")
    public EnrichedLeadResponse enrichInternationalLead(
        @McpToolParam(description = "Lead ID from previous search results", required = true) String leadId,
        @McpToolParam(description = "Company name for enrichment", required = true) String companyName,
        @McpToolParam(description = "Company website URL", required = false) String website,
        @McpToolParam(description = "Data sources for enrichment comma-separated", required = false) String sources
    ) {
        var parsedSources = inputParser.parseSources(sources);
        var leadData = inputParser.buildLeadDataForEnrichment(leadId, companyName, website);
        log.debug("MCP enrich_international_lead called: leadId={} company={} sources={}", leadId, companyName, parsedSources);
        var response = EnrichedLeadResponse.fromDomain(internationalSearchService.enrichLead(leadData, parsedSources));
        log.info("Lead enriched via MCP: leadId={} company={} enrichmentScore={} contacts={}", leadId, companyName, response.enrichmentScore(), response.contacts().size());
        return response;
    }

    @McpTool(name = "optimize_search_strategy", description = "Generate AI-driven search strategy recommendations for specific markets considering budget constraints, quality requirements, and optimal source selection.")
    public SearchStrategyResponse optimizeSearchStrategy(
        @McpToolParam(description = "Target market for optimization", required = true) String market,
        @McpToolParam(description = "Available budget in USD", required = true) String budget,
        @McpToolParam(description = "Minimum quality threshold (0.0-1.0)", required = true) String qualityThreshold
    ) {
        var normalizedMarket = inputParser.normalizeMarket(market);
        var parsedBudget = inputParser.parseRequiredDouble(budget, "budget");
        var parsedQualityThreshold = inputParser.parseRequiredDouble(qualityThreshold, "qualityThreshold");
        log.debug("MCP optimize_search_strategy called: market={} budget={} qualityThreshold={}", normalizedMarket, parsedBudget, parsedQualityThreshold);
        var response = SearchStrategyResponse.fromDomain(internationalSearchService.optimizeStrategy(normalizedMarket, parsedBudget, parsedQualityThreshold));
        log.info("Search strategy optimized via MCP: market={} recommendedSources={} estimatedCost={} estimatedQuality={}", normalizedMarket, response.recommendedSources().size(), response.estimatedCost(), response.estimatedQuality());
        return response;
    }

    @McpTool(name = "analyze_market_coverage", description = "Perform comprehensive market coverage analysis including competitor assessment, uncovered segments identification, and opportunity mapping for strategic planning.")
    public MarketCoverageResponse analyzeMarketCoverage(
        @McpToolParam(description = "Target country for analysis", required = true) String country,
        @McpToolParam(description = "Known competitors comma-separated", required = false) String competitors
    ) {
        var normalizedCountry = inputParser.normalizeRequired(country, "Country");
        var parsedCompetitors = inputParser.parseCompetitors(competitors);
        log.debug("MCP analyze_market_coverage called: country={} competitors={}", normalizedCountry, parsedCompetitors.size());
        var response = MarketCoverageResponse.fromDomain(internationalSearchService.analyzeMarketCoverage(normalizedCountry, parsedCompetitors));
        log.info("Market coverage analyzed via MCP: country={} marketSize={} coverage={}% opportunities={}", normalizedCountry, response.totalMarketSize(), (int) (response.coveredMarketShare() * 100), response.opportunities().size());
        return response;
    }
}





