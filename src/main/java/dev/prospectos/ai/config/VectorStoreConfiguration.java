package dev.prospectos.ai.config;

import dev.prospectos.ai.vector.TextEmbeddingService;
import dev.prospectos.ai.vector.EmbeddingModelDescriptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.document.Document;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration for VectorStore beans with intelligent fallbacks.
 */
@Configuration
@Slf4j
public class VectorStoreConfiguration {

    /**
     * Mock TextEmbeddingService for tests - simple and reliable.
     */
    @Bean
    @Profile("test")
    @ConditionalOnMissingBean(TextEmbeddingService.class)
    public TextEmbeddingService testTextEmbeddingService() {
        log.info("Creating Mock TextEmbeddingService for test environment");
        return new MockTextEmbeddingService();
    }


    /**
     * Simple Mock TextEmbeddingService for tests.
     */
    private static class MockTextEmbeddingService implements TextEmbeddingService {
        @Override
        public float[] embed(String text) {
            // Return simple deterministic embedding based on text hash
            int hash = text != null ? text.hashCode() : 0;
            return new float[] {
                Math.abs(hash % 100) / 100.0f,
                Math.abs((hash >> 8) % 100) / 100.0f,
                Math.abs((hash >> 16) % 100) / 100.0f,
                Math.abs((hash >> 24) % 100) / 100.0f,
                0.5f // constant component
            };
        }

        @Override
        public EmbeddingModelDescriptor descriptor() {
            return new EmbeddingModelDescriptor("mock-model", 5);
        }
    }

    /**
     * Production VectorStore for PgVector - only created when conditions are met.
     * For now, this is a placeholder since the main VectorIndex system works well.
     */
    @Bean
    @Profile("!test")
    @ConditionalOnProperty(
        prefix = "prospectos.vectorization",
        name = "backend",
        havingValue = "pgvector"
    )
    @ConditionalOnMissingBean(VectorStore.class)
    @ConditionalOnBean(EmbeddingModel.class)
    public VectorStore productionVectorStore(EmbeddingModel embeddingModel) {
        log.info("Creating fallback SimpleVectorStore for production environment");
        log.warn("PgVector VectorStore integration not yet implemented, using SimpleVectorStore");
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
