package dev.prospectos.infrastructure.mcp.service;

import java.util.List;

import dev.prospectos.api.mcp.QueryMetricsRecorder;

public final class QueryMetricsExecutionTracker {

    private QueryMetricsExecutionTracker() {
    }

    public static <T> List<T> track(QueryMetricsRecorder recorder, String provider, QueryOperation<T> operation) {
        return track(recorder, provider, provider, operation);
    }

    public static <T> List<T> track(
        QueryMetricsRecorder recorder,
        String provider,
        String operationName,
        QueryOperation<T> operation
    ) {
        long start = System.currentTimeMillis();
        try {
            List<T> results = operation.execute();
            recorder.recordExecution(provider, operationName, System.currentTimeMillis() - start, true, results.size());
            return results;
        } catch (RuntimeException exception) {
            recorder.recordExecution(provider, operationName, System.currentTimeMillis() - start, false, 0);
            throw exception;
        }
    }

    @FunctionalInterface
    public interface QueryOperation<T> {
        List<T> execute();
    }
}
