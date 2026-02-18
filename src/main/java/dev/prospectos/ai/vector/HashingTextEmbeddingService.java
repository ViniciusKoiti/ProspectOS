package dev.prospectos.ai.vector;

import dev.prospectos.ai.config.VectorizationProperties;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Deterministic in-memory embedding service based on token hashing.
 * Useful for local development and test-safe semantic ranking.
 */
@Service
public class HashingTextEmbeddingService implements TextEmbeddingService {

    private final EmbeddingModelDescriptor descriptor;

    public HashingTextEmbeddingService(VectorizationProperties properties) {
        this.descriptor = new EmbeddingModelDescriptor(properties.modelId(), properties.embeddingDimension());
    }

    @Override
    public float[] embed(String text) {
        float[] vector = new float[descriptor.dimensions()];
        if (text == null || text.isBlank()) {
            return vector;
        }

        String[] tokens = text.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+");
        for (String token : tokens) {
            if (token == null || token.isBlank()) {
                continue;
            }
            int hash = token.hashCode();
            int index = Math.floorMod(hash, descriptor.dimensions());
            int sign = (Integer.rotateLeft(hash, 13) & 1) == 0 ? 1 : -1;
            vector[index] += sign;
        }

        normalize(vector);
        return vector;
    }

    @Override
    public EmbeddingModelDescriptor descriptor() {
        return descriptor;
    }

    private void normalize(float[] vector) {
        double norm = 0.0d;
        for (float value : vector) {
            norm += value * value;
        }
        if (norm == 0.0d) {
            return;
        }
        float scale = (float) (1.0d / Math.sqrt(norm));
        for (int i = 0; i < vector.length; i++) {
            vector[i] = vector[i] * scale;
        }
    }
}
