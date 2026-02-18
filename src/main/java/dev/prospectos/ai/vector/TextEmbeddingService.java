package dev.prospectos.ai.vector;

/**
 * Converts text into fixed-size embedding vectors.
 */
public interface TextEmbeddingService {

    float[] embed(String text);

    EmbeddingModelDescriptor descriptor();
}
