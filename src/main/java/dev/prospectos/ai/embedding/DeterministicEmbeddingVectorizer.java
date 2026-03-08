package dev.prospectos.ai.embedding;

import java.util.Locale;

final class DeterministicEmbeddingVectorizer {

    float[] embed(String text, int dimensions) {
        float[] vector = new float[dimensions];
        if (text == null || text.isBlank()) {
            return vector;
        }
        String[] tokens = text.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+");
        for (String token : tokens) {
            if (token == null || token.isBlank()) {
                continue;
            }
            int hash = token.hashCode();
            int index = Math.floorMod(hash, dimensions);
            vector[index] += ((Integer.rotateLeft(hash, 7) & 1) == 0) ? 1.0f : -1.0f;
        }
        return normalize(vector);
    }

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
}
