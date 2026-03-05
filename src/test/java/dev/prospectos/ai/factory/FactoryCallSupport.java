package dev.prospectos.ai.factory;

import dev.prospectos.ai.client.LLMClient;
import dev.prospectos.ai.client.LLMProvider;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

final class FactoryCallSupport {

    private FactoryCallSupport() {
    }

    static <T> T invoke(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier).join();
    }

    static LLMClient bestAvailableClient(LLMClientFactory factory) {
        return CompletableFuture.supplyAsync(factory::createBestAvailableClient).join();
    }

    static LLMClient bestAvailableScoringClient(LLMClientFactory factory) {
        return CompletableFuture.supplyAsync(() ->
            (LLMClient) ReflectionTestUtils.invokeMethod(factory, "createBestAvailableScoringClient")
        ).join();
    }

    static LLMClient createClient(LLMClientFactory factory, LLMProvider provider) {
        return CompletableFuture.supplyAsync(() -> factory.createClient(provider)).join();
    }
}
