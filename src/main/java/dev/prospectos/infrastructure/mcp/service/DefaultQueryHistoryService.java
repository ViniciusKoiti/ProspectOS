package dev.prospectos.infrastructure.mcp.service;

import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.mcp.MarketAnalysisData;
import dev.prospectos.api.mcp.ProviderPerformanceData;
import dev.prospectos.api.mcp.QueryHistoryData;
import dev.prospectos.api.mcp.QueryHistoryService;
import dev.prospectos.api.mcp.QueryTimeWindow;
import dev.prospectos.infrastructure.mcp.config.ConditionalOnMcpEnabled;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnMcpEnabled
public class DefaultQueryHistoryService implements QueryHistoryService {

    private final ObservedQueryMetricsService queryMetricsService;
    private final QueryHistoryExecutionGenerator executionGenerator = new QueryHistoryExecutionGenerator();
    private final QueryHistoryPerformanceGenerator performanceGenerator = new QueryHistoryPerformanceGenerator();
    private final QueryHistoryMarketAnalysisGenerator marketAnalysisGenerator;

    public DefaultQueryHistoryService(ObservedQueryMetricsService queryMetricsService, CompanyDataService companyDataService) {
        this.queryMetricsService = queryMetricsService;
        this.marketAnalysisGenerator = new QueryHistoryMarketAnalysisGenerator(companyDataService);
    }

    @Override
    public QueryHistoryData getQueryHistory(QueryTimeWindow timeWindow, String provider) {
        log.debug("Building query history for timeWindow={} provider={}", timeWindow.value(), provider);
        return executionGenerator.generate(timeWindow, provider, queryMetricsService.observations(timeWindow, provider));
    }

    @Override
    public ProviderPerformanceData getProviderPerformance(String provider, String metric) {
        log.debug("Building provider performance for provider={} metric={}", provider, metric);
        return performanceGenerator.generate(provider, metric, queryMetricsService.observations(QueryTimeWindow.TWENTY_FOUR_HOURS, provider));
    }

    @Override
    public MarketAnalysisData getMarketAnalysis(String country, String industry) {
        log.debug("Building market analysis for country={} industry={}", country, industry);
        return marketAnalysisGenerator.generate(country, industry);
    }
}




