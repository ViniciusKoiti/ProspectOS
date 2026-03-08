package dev.prospectos.ai.client;

import java.util.function.Function;

import org.slf4j.Logger;

final class AIWebSearchRetryExecutor {

    private AIWebSearchRetryExecutor() {
    }

    static <T> T runWithRetries(
        Logger log,
        String operation,
        String target,
        int maxRetries,
        RetryTask<T> task,
        Function<Exception, T> onFailure
    ) {
        Exception lastException = null;
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                logAttempt(log, operation, target, attempt, maxRetries);
                return task.execute();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxRetries) {
                    log.warn(
                        "AI {} failed for {} (attempt {}/{}). Error: {}",
                        operation,
                        target,
                        attempt + 1,
                        maxRetries + 1,
                        e.getMessage()
                    );
                    try {
                        sleepBeforeRetry(attempt + 1);
                    } catch (InterruptedException interruptedException) {
                        Thread.currentThread().interrupt();
                        lastException = interruptedException;
                        break;
                    }
                }
            }
        }
        log.error("AI {} failed for {} after {} attempts", operation, target, maxRetries + 1, lastException);
        return onFailure.apply(lastException);
    }

    private static void logAttempt(Logger log, String operation, String target, int attempt, int maxRetries) {
        if (attempt > 0) {
            log.info("Retrying AI {} for {} (attempt {}/{})", operation, target, attempt, maxRetries);
        } else {
            log.info("AI {} for {}", operation, target);
        }
    }

    private static void sleepBeforeRetry(int attempt) throws InterruptedException {
        Thread.sleep(1000L * attempt);
    }

    @FunctionalInterface
    interface RetryTask<T> {
        T execute() throws Exception;
    }
}
