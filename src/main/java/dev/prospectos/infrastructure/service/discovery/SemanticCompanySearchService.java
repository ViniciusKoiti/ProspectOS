package dev.prospectos.infrastructure.service.discovery;

import dev.prospectos.ai.config.VectorizationProperties;
import dev.prospectos.ai.vector.TextEmbeddingService;
import dev.prospectos.ai.vector.VectorIndex;
import dev.prospectos.ai.vector.VectorSearchMatch;
import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.dto.CompanyDTO;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides semantic search over registered companies using vector similarity.
 */
@Service
public class SemanticCompanySearchService {

    private final CompanyDataService companyDataService;
    private final TextEmbeddingService embeddingService;
    private final VectorIndex vectorIndex;
    private final VectorizationProperties properties;
    private final Object indexLock = new Object();

    public SemanticCompanySearchService(
        CompanyDataService companyDataService,
        TextEmbeddingService embeddingService,
        VectorIndex vectorIndex,
        VectorizationProperties properties
    ) {
        this.companyDataService = companyDataService;
        this.embeddingService = embeddingService;
        this.vectorIndex = vectorIndex;
        this.properties = properties;

        if (embeddingService.descriptor().dimensions() != vectorIndex.dimensions()) {
            throw new IllegalStateException(
                "Embedding/vector dimension mismatch. Embedding: "
                    + embeddingService.descriptor().dimensions()
                    + ", VectorIndex: "
                    + vectorIndex.dimensions()
            );
        }
    }

    public List<SemanticCompanyMatch> search(String query, Integer requestedTopK) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        int topK = requestedTopK == null ? properties.topK() : requestedTopK;
        if (topK <= 0) {
            return List.of();
        }

        List<VectorSearchMatch> matches;
        Map<String, CompanyDTO> indexed;
        synchronized (indexLock) {
            indexed = rebuildIndex();
            float[] queryVector = embeddingService.embed(query.trim());
            matches = vectorIndex.similaritySearch(queryVector, topK, properties.minSimilarity());
        }

        return matches.stream()
            .map(match -> {
                CompanyDTO company = indexed.get(match.id());
                if (company == null) {
                    return null;
                }
                return new SemanticCompanyMatch(company, match.similarity());
            })
            .filter(match -> match != null)
            .toList();
    }

    private Map<String, CompanyDTO> rebuildIndex() {
        vectorIndex.clear();
        Map<String, CompanyDTO> indexed = new LinkedHashMap<>();
        for (CompanyDTO company : companyDataService.findAllCompanies()) {
            if (company == null || company.id() == null) {
                continue;
            }
            String id = String.valueOf(company.id());
            String content = toSemanticContent(company);
            float[] vector = embeddingService.embed(content);
            vectorIndex.upsert(id, vector, Map.of(
                "companyId", company.id(),
                "modelId", embeddingService.descriptor().modelId()
            ));
            indexed.put(id, company);
        }
        return indexed;
    }

    private String toSemanticContent(CompanyDTO company) {
        StringBuilder builder = new StringBuilder();
        append(builder, company.name());
        append(builder, company.industry());
        append(builder, company.description());
        append(builder, company.location());
        append(builder, company.website());
        return builder.toString().trim();
    }

    private void append(StringBuilder builder, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (!builder.isEmpty()) {
            builder.append(". ");
        }
        builder.append(value.trim());
    }
}
