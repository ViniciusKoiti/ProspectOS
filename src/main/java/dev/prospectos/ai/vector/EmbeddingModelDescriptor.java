package dev.prospectos.ai.vector;

/**
 * Describes the active embedding model contract.
 */
public record EmbeddingModelDescriptor(
    String modelId,
    int dimensions
) {
}
