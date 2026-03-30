package dev.prospectos.infrastructure.mcp.service;

import dev.prospectos.api.mcp.MarketAnalysisData;
import dev.prospectos.api.mcp.ProviderPerformanceData;
import dev.prospectos.api.mcp.QueryHistoryData;
import dev.prospectos.api.mcp.QueryHistoryService;
import dev.prospectos.api.mcp.QueryTimeWindow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
@Profile("mcp")
public class DefaultQueryHistoryService implements QueryHistoryService {

    private final QueryHistoryExecutionGenerator executionGenerator;
    private final QueryHistoryPerformanceGenerator performanceGenerator;
    private final QueryHistoryMarketAnalysisGenerator marketAnalysisGenerator;

    public DefaultQueryHistoryService() {
        var random = new Random();
        this.executionGenerator = new QueryHistoryExecutionGenerator(random);
        this.performanceGenerator = new QueryHistoryPerformanceGenerator(random);
        this.marketAnalysisGenerator = new QueryHistoryMarketAnalysisGenerator(random);
    }

    @Override
    public QueryHistoryData getQueryHistory(QueryTimeWindow timeWindow, String provider) {
        log.debug("Generating query history for timeWindow={} provider={}", timeWindow.value(), provider);
        return executionGenerator.generate(timeWindow, provider);
    }

    @Override
    public ProviderPerformanceData getProviderPerformance(String provider, String metric) {
        log.debug("Generating provider performance for provider={} metric={}", provider, metric);
        return performanceGenerator.generate(provider, metric);
    }

    @Override
    public MarketAnalysisData getMarketAnalysis(String country, String industry) {
        log.debug("Generating market analysis for country={} industry={}", country, industry);
        return marketAnalysisGenerator.generate(country, industry);
    }
}
