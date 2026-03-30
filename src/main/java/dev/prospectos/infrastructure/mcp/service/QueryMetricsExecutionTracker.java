package dev.prospectos.infrastructure.mcp.service;

import java.util.List;

import dev.prospectos.api.mcp.QueryMetricsRecorder;

public final class QueryMetricsExecutionTracker {

    private QueryMetricsExecutionTracker() {
    }

    public static <T> List<T> track(QueryMetricsRecorder recorder, String provider, QueryOperation<T> operation) {
        long startedAt = System.nanoTime();
        try {
            List<T> results = operation.execute();
            recorder.recordExecution(provider, elapsedMs(startedAt), true, results.size());
            return results;
        } catch (RuntimeException exception) {
            recorder.recordExecution(provider, elapsedMs(startedAt), false, 0);
            throw exception;
        }
    }

    private static long elapsedMs(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000L;
    }

    @FunctionalInterface
    public interface QueryOperation<T> {
        List<T> execute();
    }
}