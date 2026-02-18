package dev.prospectos.infrastructure.service.discovery;

import dev.prospectos.ai.config.VectorizationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Event listener that updates vectors incrementally when company data changes.
 */
@Component
@Slf4j
public class CompanyVectorIndexingListener {

    private final CompanyVectorIndexingService indexingService;
    private final VectorizationProperties properties;

    public CompanyVectorIndexingListener(
        CompanyVectorIndexingService indexingService,
        VectorizationProperties properties
    ) {
        this.indexingService = indexingService;
        this.properties = properties;
    }

    @EventListener
    public void onCompanyChanged(CompanyVectorReindexRequested event) {
        if (event == null || event.companyId() == null) {
            return;
        }
        try {
            indexingService.reindexCompany(event.companyId());
        } catch (Exception ex) {
            // Vector indexing failure must not break company lifecycle operations.
            log.error(
                "Vector reindex failed (companyId={}, backend={}, operation=reindex): {}",
                event.companyId(),
                properties.backend(),
                ex.getMessage(),
                ex
            );
        }
    }
}
