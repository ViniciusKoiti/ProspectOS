package dev.prospectos.ai.client;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AIWebSearchTimeoutExecutorTest {

    @Test
    void executeReturnsResultWhenTaskCompletesWithinTimeout() throws Exception {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Logger logger = mock(Logger.class);
        try {
            String result = AIWebSearchTimeoutExecutor.execute(
                () -> "ok",
                Duration.ofMillis(500),
                executorService,
                logger
            );

            assertEquals("ok", result);
        } finally {
            executorService.shutdownNow();
        }
    }

    @Test
    void executeCancelsTaskAndLogsWarningWhenTimeoutIsReached() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Logger logger = mock(Logger.class);
        AtomicBoolean interrupted = new AtomicBoolean(false);
        try {
            TimeoutException exception = assertThrows(
                TimeoutException.class,
                () -> AIWebSearchTimeoutExecutor.execute(
                    () -> {
                        try {
                            Thread.sleep(5_000);
                            return "late";
                        } catch (InterruptedException e) {
                            interrupted.set(true);
                            Thread.currentThread().interrupt();
                            return "interrupted";
                        }
                    },
                    Duration.ofMillis(30),
                    executorService,
                    logger
                )
            );

            assertTrue(exception.getMessage() == null || exception.getMessage().isBlank());
            verify(logger).warn("AI request timed out after {}ms", 30L);
            assertTrue(interrupted.get());
        } finally {
            executorService.shutdownNow();
        }
    }

    @Test
    void executePropagatesExecutionExceptionFromTask() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Logger logger = mock(Logger.class);
        try {
            ExecutionException exception = assertThrows(
                ExecutionException.class,
                () -> AIWebSearchTimeoutExecutor.execute(
                    () -> {
                        throw new IllegalStateException("boom");
                    },
                    Duration.ofMillis(300),
                    executorService,
                    logger
                )
            );

            assertTrue(exception.getCause() instanceof IllegalStateException);
            assertEquals("boom", exception.getCause().getMessage());
        } finally {
            executorService.shutdownNow();
        }
    }
}
