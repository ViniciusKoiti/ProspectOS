package dev.prospectos.infrastructure.service.discovery;

import java.util.List;

final class ApifyResponseMapper {

    private final String sourceName;

    ApifyResponseMapper(String sourceName) {
        this.sourceName = sourceName;
    }

    List<DiscoveredLeadCandidate> toCandidates(ApifyDatasetItem[] items, int limit) {
        if (items == null || items.length == 0) {
            return List.of();
        }
        return java.util.Arrays.stream(items)
            .limit(limit)
            .map(this::toCandidate)
            .toList();
    }

    private DiscoveredLeadCandidate toCandidate(ApifyDatasetItem item) {
        String name = firstNonBlank(item.title(), item.name());
        String location = firstNonBlank(item.fullAddress(), item.address());
        String phone = firstNonBlank(item.phone(), item.phoneUnformatted());
        String industry = firstNonBlank(item.categoryName(), item.category(), firstOf(item.categories()), "local business");
        List<String> contacts = phone == null ? List.of() : List.of(phone);
        return new DiscoveredLeadCandidate(name, item.website(), industry, location, location, contacts, sourceName);
    }

    private String firstOf(List<String> values) {
        return values == null || values.isEmpty() ? null : values.getFirst();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }
}
