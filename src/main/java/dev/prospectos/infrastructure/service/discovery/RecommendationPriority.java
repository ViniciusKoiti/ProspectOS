package dev.prospectos.infrastructure.service.discovery;

import java.util.List;

final class RecommendationPriority {

    private static final List<String> ORDER = List.of(
        "google-places",
        "amazon-location",
        "scraper",
        "llm-discovery",
        "open-cnpj",
        "cnpj-ws",
        "vector-company",
        "in-memory"
    );

    double priorityOf(String source) {
        int index = ORDER.indexOf(source);
        return index < 0 ? 0.0d : 100.0d - index;
    }

    boolean isProductionSource(String source) {
        return !"in-memory".equals(source) && !"vector-company".equals(source);
    }
}
