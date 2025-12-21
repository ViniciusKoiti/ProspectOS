package dev.prospectos.ai.client.mock;

import dev.prospectos.ai.mock.MockStructuredResponseFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * Factory dedicated to creating AI response mocks.
 * Centralizes all logic for structured mock creation.
 */
@Slf4j
public class MockResponseFactory {
    
    /**
     * Creates a mock response based on the requested type.
     */
    public static <T> T createMockResponse(Class<T> responseClass, String providerName) {
        log.debug("Creating mock for {} from {}", responseClass.getSimpleName(), providerName);
        return MockStructuredResponseFactory.create(responseClass, providerName);
    }
}
