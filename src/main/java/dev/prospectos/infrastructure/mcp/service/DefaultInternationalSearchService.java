package dev.prospectos.infrastructure.mcp.service;

import dev.prospectos.api.mcp.EnrichedLeadData;
import dev.prospectos.api.mcp.InternationalSearchResult;
import dev.prospectos.api.mcp.InternationalSearchService;
import dev.prospectos.api.mcp.LeadData;
import dev.prospectos.api.mcp.LeadSearchCriteria;
import dev.prospectos.api.mcp.MarketCoverageAnalysis;
import dev.prospectos.api.mcp.SearchStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
@Profile("mcp")
public class DefaultInternationalSearchService implements InternationalSearchService {

    private final InternationalLeadGenerator leadGenerator;
    private final InternationalSearchAssessmentFactory assessmentFactory;
    private final InternationalLeadEnricher leadEnricher;
    private final InternationalStrategyOptimizer strategyOptimizer;
    private final InternationalMarketCoverageAnalyzer marketCoverageAnalyzer;

    public DefaultInternationalSearchService() {
        var random = new Random();
        var fixtures = new InternationalLeadFixtures(random);
        this.leadGenerator = new InternationalLeadGenerator(random, fixtures);
        this.assessmentFactory = new InternationalSearchAssessmentFactory(random);
        this.leadEnricher = new InternationalLeadEnricher(random);
        this.strategyOptimizer = new InternationalStrategyOptimizer(random);
        this.marketCoverageAnalyzer = new InternationalMarketCoverageAnalyzer(random);
    }

    @Override
    public InternationalSearchResult searchLeads(String country, String industry, LeadSearchCriteria criteria) {
        log.debug("Searching international leads: country={} industry={} maxResults={}", country, industry, criteria.maxResults());
        var leads = leadGenerator.generate(country, industry, criteria);
        var result = new InternationalSearchResult(country, industry, leads, assessmentFactory.quality(leads), assessmentFactory.cost(leads, criteria), assessmentFactory.sources(criteria));
        log.info("International search completed: country={} industry={} leads={} quality={} cost=${}", country, industry, leads.size(), result.quality().overallScore(), result.totalCost());
        return result;
    }

    @Override
    public EnrichedLeadData enrichLead(LeadData leadData, List<String> sources) {
        log.debug("Enriching lead: company={} sources={}", leadData.companyName(), sources);
        var enrichedData = leadEnricher.enrich(leadData, sources);
        log.info("Lead enriched: company={} enrichmentScore={} contacts={}", leadData.companyName(), enrichedData.enrichmentScore(), enrichedData.contacts().size());
        return enrichedData;
    }

    @Override
    public SearchStrategy optimizeStrategy(String market, double budget, double qualityThreshold) {
        log.debug("Optimizing search strategy: market={} budget=${} qualityThreshold={}", market, budget, qualityThreshold);
        var strategy = strategyOptimizer.optimize(market, budget, qualityThreshold);
        log.info("Search strategy optimized: market={} sources={} estimatedCost=${} estimatedQuality={}", market, strategy.recommendedSources().size(), strategy.estimatedCost(), strategy.estimatedQuality());
        return strategy;
    }

    @Override
    public MarketCoverageAnalysis analyzeMarketCoverage(String country, List<String> competitors) {
        log.debug("Analyzing market coverage: country={} competitors={}", country, competitors.size());
        var analysis = marketCoverageAnalyzer.analyze(country, competitors);
        log.info("Market coverage analyzed: country={} marketSize={} coverage={}% opportunities={}", country, analysis.totalMarketSize(), (int) (analysis.coveredMarketShare() * 100), analysis.opportunities().size());
        return analysis;
    }
}
