package dev.prospectos.ai.client;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.slf4j.Logger;

final class AIWebSearchTimeoutExecutor {

    private AIWebSearchTimeoutExecutor() {
    }

    static <T> T execute(Supplier<T> task, Duration timeout, ExecutorService executorService, Logger log)
        throws TimeoutException, ExecutionException, InterruptedException {
        Future<T> future = executorService.submit(task::get);
        try {
            return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            log.warn("AI request timed out after {}ms", timeout.toMillis());
            throw e;
        }
    }
}
