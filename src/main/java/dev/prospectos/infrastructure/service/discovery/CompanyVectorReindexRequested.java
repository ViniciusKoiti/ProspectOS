package dev.prospectos.infrastructure.service.discovery;

/**
 * Event fired when a company vector should be refreshed in the vector index.
 */
public record CompanyVectorReindexRequested(Long companyId) {
}
