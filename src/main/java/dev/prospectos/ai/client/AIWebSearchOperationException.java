package dev.prospectos.ai.client;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

final class AIWebSearchOperationException extends RuntimeException {

    private final String userMessage;

    AIWebSearchOperationException(String operation, String target, int attempts, Exception cause) {
        super(buildTechnicalMessage(operation, target, attempts, cause), unwrap(cause));
        this.userMessage = buildUserMessage(operation, target, attempts, cause);
    }

    String userMessage() {
        return userMessage;
    }

    private static String buildTechnicalMessage(String operation, String target, int attempts, Exception cause) {
        Throwable rootCause = unwrap(cause);
        String causeMessage = rootCause != null && rootCause.getMessage() != null
            ? rootCause.getMessage()
            : "unknown error";
        return "AI " + operation + " failed for " + target + " after " + attempts + " attempts: " + causeMessage;
    }

    private static String buildUserMessage(String operation, String target, int attempts, Exception cause) {
        Throwable rootCause = unwrap(cause);
        if (rootCause instanceof TimeoutException) {
            return "AI " + operation + " timed out for " + target + " after " + attempts + " attempts";
        }
        if (rootCause instanceof InterruptedException) {
            return "AI " + operation + " was interrupted for " + target;
        }
        String causeMessage = rootCause != null && rootCause.getMessage() != null
            ? rootCause.getMessage()
            : "unknown error";
        return "AI " + operation + " failed for " + target + " after " + attempts + " attempts: " + causeMessage;
    }

    private static Throwable unwrap(Exception cause) {
        if (cause instanceof ExecutionException executionException && executionException.getCause() != null) {
            return executionException.getCause();
        }
        return cause;
    }
}
