package dev.prospectos.infrastructure.service.discovery;

import dev.prospectos.ai.config.VectorizationProperties;
import dev.prospectos.ai.vector.TextEmbeddingService;
import dev.prospectos.ai.vector.VectorIndex;
import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.dto.CompanyDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

/**
 * Maintains company documents inside the configured vector index.
 */
@Service
@Slf4j
public class CompanyVectorIndexingService {

    private final CompanyDataService companyDataService;
    private final TextEmbeddingService embeddingService;
    private final VectorIndex vectorIndex;
    private final VectorizationProperties properties;

    public CompanyVectorIndexingService(
        CompanyDataService companyDataService,
        TextEmbeddingService embeddingService,
        VectorIndex vectorIndex,
        VectorizationProperties properties
    ) {
        this.companyDataService = companyDataService;
        this.embeddingService = embeddingService;
        this.vectorIndex = vectorIndex;
        this.properties = properties;
    }

    public void reindexCompany(Long companyId) {
        if (companyId == null) {
            return;
        }

        CompanyDTO company = companyDataService.findCompany(companyId);
        if (company == null) {
            vectorIndex.delete(documentId(companyId));
            return;
        }

        vectorIndex.upsert(
            documentId(companyId),
            toSemanticContent(company),
            Map.of(
                "companyId", company.id(),
                "modelId", embeddingService.descriptor().modelId(),
                "backend", properties.backend()
            )
        );
        log.debug("Indexed company {} into vector backend {}", companyId, properties.backend());
    }

    private String documentId(Long companyId) {
        return UUID.nameUUIDFromBytes(("company:" + companyId).getBytes(StandardCharsets.UTF_8)).toString();
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
