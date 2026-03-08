package dev.prospectos.ai.embedding;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TestDeterministicEmbeddingModelTest {

    @Test
    void embedIsDeterministicForSameInput() {
        TestDeterministicEmbeddingModel model = new TestDeterministicEmbeddingModel(32);

        float[] first = model.embed(new Document("Acme growth signals"));
        float[] second = model.embed(new Document("Acme growth signals"));

        assertArrayEquals(first, second);
        assertEquals(32, first.length);
    }

    @Test
    void embedReturnsZeroVectorForBlankOrNullDocument() {
        TestDeterministicEmbeddingModel model = new TestDeterministicEmbeddingModel(16);

        float[] blank = model.embed(new Document("   "));
        float[] nullDocument = model.embed((Document) null);

        assertEquals(16, blank.length);
        assertEquals(16, nullDocument.length);
        assertArrayEquals(new float[16], blank);
        assertArrayEquals(new float[16], nullDocument);
    }

    @Test
    void exposesConfiguredDimensions() {
        TestDeterministicEmbeddingModel model = new TestDeterministicEmbeddingModel(24);

        assertEquals(24, model.getDimensions());
        assertNotNull(model.call(null));
    }
}
