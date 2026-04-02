package dev.prospectos.api.mcp;

/**
 * Provides historical query data access for MCP resources.
 */
public interface QueryHistoryService {

    QueryHistoryData getQueryHistory(QueryTimeWindow timeWindow, String provider);

    ProviderPerformanceData getProviderPerformance(String provider, String metric);

    MarketAnalysisData getMarketAnalysis(String country, String industry);
}
