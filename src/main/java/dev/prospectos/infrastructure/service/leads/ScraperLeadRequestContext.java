package dev.prospectos.infrastructure.service.leads;

import java.util.List;

record ScraperLeadRequestContext(
    int limit,
    List<String> sources,
    String query,
    String scraperQuery
) {
    ScraperLeadRequestContext {
        sources = sources == null ? List.of() : List.copyOf(sources);
    }
}
