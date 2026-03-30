package dev.prospectos.api.mcp;

/**
 * Records query execution events for later aggregation.
 */
public interface QueryMetricsRecorder {

    void recordExecution(String provider, long durationMs, boolean success, int resultCount);
}
