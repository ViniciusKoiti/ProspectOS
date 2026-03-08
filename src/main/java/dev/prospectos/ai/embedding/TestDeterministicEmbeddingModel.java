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
    private final DeterministicEmbeddingVectorizer vectorizer;

    public TestDeterministicEmbeddingModel() {
        this.dimensions = 64;
        this.vectorizer = new DeterministicEmbeddingVectorizer();
        log.info("TestDeterministicEmbeddingModel initialized with {} dimensions", dimensions);
    }

    public TestDeterministicEmbeddingModel(int dimensions) {
        this.dimensions = dimensions;
        this.vectorizer = new DeterministicEmbeddingVectorizer();
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

    private float[] embedText(String text) {
        return vectorizer.embed(text, dimensions);
    }

    public int getDimensions() {
        return dimensions;
    }
}
