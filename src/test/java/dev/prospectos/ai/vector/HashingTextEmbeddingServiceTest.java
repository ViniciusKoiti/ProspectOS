package dev.prospectos.ai.vector;

import dev.prospectos.ai.config.VectorizationProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HashingTextEmbeddingServiceTest {

    @Test
    void embed_ReturnsConfiguredDimensionAndDeterministicVector() {
        VectorizationProperties properties = new VectorizationProperties("hashing-v1", 64, 5, 0.2d);
        HashingTextEmbeddingService service = new HashingTextEmbeddingService(properties);

        float[] first = service.embed("agile scrum kanban software engineering");
        float[] second = service.embed("agile scrum kanban software engineering");

        assertEquals(64, first.length);
        assertArrayEquals(first, second);
        assertEquals("hashing-v1", service.descriptor().modelId());
        assertEquals(64, service.descriptor().dimensions());
    }

    @Test
    void embed_SupportsUnicodeTokensForPortugueseText() {
        VectorizationProperties properties = new VectorizationProperties("hashing-v1", 64, 5, 0.2d);
        HashingTextEmbeddingService service = new HashingTextEmbeddingService(properties);

        float[] vector = service.embed("metodologias ágeis para desenvolvimento de software");

        float sumAbs = 0.0f;
        for (float value : vector) {
            sumAbs += Math.abs(value);
        }
        assertEquals(64, vector.length);
        org.junit.jupiter.api.Assertions.assertTrue(sumAbs > 0.0f);
    }
}
