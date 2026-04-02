package dev.prospectos.api.mcp;

/**
 * Records query execution events for later aggregation.
 */
public interface QueryMetricsRecorder {

    void recordExecution(String provider, long durationMs, boolean success, int resultCount);

    default void recordExecution(String provider, String operation, long durationMs, boolean success, int resultCount) {
        recordExecution(provider, durationMs, success, resultCount);
    }
}
