package dev.prospectos.api.mcp;

/**
 * Supplies aggregated query metrics for MCP tools.
 */
public interface QueryMetricsService {

    QueryMetricsSnapshot getMetrics(QueryTimeWindow timeWindow, String provider);
}
