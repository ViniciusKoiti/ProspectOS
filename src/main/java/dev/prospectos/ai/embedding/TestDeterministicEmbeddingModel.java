package dev.prospectos.ai.embedding;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Deterministic EmbeddingModel implementation for testing purposes.
 * 
 * This component provides consistent, reproducible embeddings based on text content hashing,
 * making it ideal for tests that require predictable results without external AI dependencies.
 * 
 * Only active in test profile when pgvector backend is enabled.
 */
@Component
@Profile("test")
@ConditionalOnProperty(
    prefix = "prospectos.vectorization",
    name = "backend",
    havingValue = "pgvector"
)
@Slf4j
public class TestDeterministicEmbeddingModel implements EmbeddingModel {

    private final int dimensions;

    /**
     * Creates a deterministic embedding model with default 64 dimensions.
     * This matches the typical dimension configuration for test environments.
     */
    public TestDeterministicEmbeddingModel() {
        this.dimensions = 64;
        log.info("TestDeterministicEmbeddingModel initialized with {} dimensions", dimensions);
    }

    /**
     * Creates a deterministic embedding model with specified dimensions.
     * 
     * @param dimensions the number of dimensions for the embedding vectors
     */
    public TestDeterministicEmbeddingModel(int dimensions) {
        this.dimensions = dimensions;
        log.debug("TestDeterministicEmbeddingModel initialized with {} dimensions", dimensions);
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        List<Embedding> embeddings = new ArrayList<>();
        List<String> instructions = request == null ? List.of() : request.getInstructions();
        for (int i = 0; i < instructions.size(); i++) {
            embeddings.add(new Embedding(embedText(instructions.get(i)), i));
        }
        return new EmbeddingResponse(embeddings);
    }

    @Override
    public float[] embed(Document document) {
        return embedText(document == null ? "" : document.getText());
    }

    /**
     * Generates deterministic embeddings based on text content.
     * Uses token-based hashing with normalization for consistent results.
     * 
     * @param text the input text to embed
     * @return normalized embedding vector
     */
    private float[] embedText(String text) {
        float[] vector = new float[dimensions];
        if (text == null || text.isBlank()) {
            return vector;
        }

        // Tokenize and process each word for deterministic embedding
        String[] tokens = text.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+");
        for (String token : tokens) {
            if (token == null || token.isBlank()) {
                continue;
            }
            int hash = token.hashCode();
            int index = Math.floorMod(hash, dimensions);
            // Use bit rotation for better distribution
            vector[index] += ((Integer.rotateLeft(hash, 7) & 1) == 0) ? 1.0f : -1.0f;
        }
        return normalize(vector);
    }

    /**
     * Normalizes the vector to unit length for cosine similarity calculations.
     * 
     * @param vector the input vector to normalize
     * @return normalized vector with unit length
     */
    private float[] normalize(float[] vector) {
        double norm = 0.0d;
        for (float value : vector) {
            norm += value * value;
        }
        if (norm == 0.0d) {
            return vector;
        }
        float scale = (float) (1.0d / Math.sqrt(norm));
        for (int i = 0; i < vector.length; i++) {
            vector[i] = vector[i] * scale;
        }
        return vector;
    }

    /**
     * Gets the dimension size of this embedding model.
     * 
     * @return the number of dimensions in embedding vectors
     */
    public int getDimensions() {
        return dimensions;
    }
}


