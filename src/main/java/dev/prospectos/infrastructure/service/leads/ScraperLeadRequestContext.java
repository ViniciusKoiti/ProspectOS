package dev.prospectos.infrastructure.service.leads;

record ScraperLeadRequestContext(
    int limit,
    String sourceName,
    String query
) {
}
