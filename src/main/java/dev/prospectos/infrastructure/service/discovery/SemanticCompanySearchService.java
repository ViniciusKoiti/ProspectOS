package dev.prospectos.infrastructure.service.discovery;

import dev.prospectos.ai.config.VectorizationProperties;
import dev.prospectos.ai.vector.VectorIndex;
import dev.prospectos.ai.vector.VectorSearchMatch;
import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.dto.CompanyDTO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Provides semantic search over registered companies using vector similarity.
 */
@Service
public class SemanticCompanySearchService {

    private final CompanyDataService companyDataService;
    private final VectorIndex vectorIndex;
    private final VectorizationProperties properties;

    public SemanticCompanySearchService(
        CompanyDataService companyDataService,
        VectorIndex vectorIndex,
        VectorizationProperties properties
    ) {
        this.companyDataService = companyDataService;
        this.vectorIndex = vectorIndex;
        this.properties = properties;
    }

    public List<SemanticCompanyMatch> search(String query, Integer requestedTopK) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        int topK = requestedTopK == null ? properties.topK() : requestedTopK;
        if (topK <= 0) {
            return List.of();
        }

        List<VectorSearchMatch> matches = vectorIndex.similaritySearch(query.trim(), topK, properties.minSimilarity());

        return matches.stream()
            .map(match -> {
                Long companyId = parseCompanyId(match);
                CompanyDTO company = companyId == null ? null : companyDataService.findCompany(companyId);
                if (company == null) {
                    return null;
                }
                return new SemanticCompanyMatch(company, match.similarity());
            })
            .filter(match -> match != null)
            .toList();
    }

    private Long parseCompanyId(VectorSearchMatch match) {
        if (match == null || match.metadata() == null) {
            return null;
        }
        Object companyId = match.metadata().get("companyId");
        if (companyId instanceof Number number) {
            return number.longValue();
        }
        if (companyId instanceof String text && !text.isBlank()) {
            try {
                return Long.parseLong(text.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        if (match.id() != null && match.id().startsWith("company:")) {
            try {
                return Long.parseLong(match.id().substring("company:".length()));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
