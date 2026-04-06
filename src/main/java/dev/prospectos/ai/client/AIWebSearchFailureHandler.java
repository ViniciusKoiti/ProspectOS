package dev.prospectos.ai.client;

import java.util.List;

final class AIWebSearchFailureHandler {

    ScrapingResponse scrapingFailure(String website, int attempts, Exception exception) {
        AIWebSearchOperationException failure = operationFailure("web search", website, attempts, exception);
        return new ScrapingResponse(false, null, failure.userMessage());
    }

    NewsResponse newsFailure(String companyName, int attempts, Exception exception) {
        AIWebSearchOperationException failure = operationFailure("news search", companyName, attempts, exception);
        return new NewsResponse(List.of(failure.userMessage()));
    }

    AIWebSearchOperationException operationFailure(String operation, String target, int attempts, Exception exception) {
        return new AIWebSearchOperationException(operation, target, attempts, exception);
    }
}
