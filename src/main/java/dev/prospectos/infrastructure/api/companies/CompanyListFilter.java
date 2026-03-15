package dev.prospectos.infrastructure.api.companies;

import java.util.List;
import java.util.Locale;

import dev.prospectos.api.dto.CompanyDTO;

final class CompanyListFilter {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;

    record Result(List<CompanyDTO> items, int totalItems) {
    }

    Result apply(
        List<CompanyDTO> source,
        String query,
        String industry,
        String location,
        Double minScore,
        Double maxScore,
        Boolean hasContact,
        Integer page,
        Integer size
    ) {
        validateScoreRange(minScore, maxScore);
        int resolvedPage = resolvePage(page, size);
        int resolvedSize = resolveSize(page, size);

        List<CompanyDTO> filtered = source.stream()
            .filter(company -> matchesQuery(company, query))
            .filter(company -> matchesContains(company.industry(), industry))
            .filter(company -> matchesContains(company.location(), location))
            .filter(company -> matchesScoreRange(company, minScore, maxScore))
            .filter(company -> matchesHasContact(company, hasContact))
            .toList();

        if (page == null && size == null) {
            return new Result(filtered, filtered.size());
        }

        int startIndex = resolvedPage * resolvedSize;
        if (startIndex >= filtered.size()) {
            return new Result(List.of(), filtered.size());
        }

        int endIndex = Math.min(startIndex + resolvedSize, filtered.size());
        return new Result(filtered.subList(startIndex, endIndex), filtered.size());
    }

    private void validateScoreRange(Double minScore, Double maxScore) {
        if (minScore != null && maxScore != null && minScore > maxScore) {
            throw new IllegalArgumentException("minScore cannot be greater than maxScore");
        }
    }

    private int resolvePage(Integer page, Integer size) {
        int resolved = page == null ? DEFAULT_PAGE : page;
        if (resolved < 0) {
            throw new IllegalArgumentException("page must be greater than or equal to 0");
        }
        if (page != null || size != null) {
            return resolved;
        }
        return DEFAULT_PAGE;
    }

    private int resolveSize(Integer page, Integer size) {
        int resolved = size == null ? DEFAULT_SIZE : size;
        if ((page != null || size != null) && resolved <= 0) {
            throw new IllegalArgumentException("size must be greater than 0");
        }
        if (page != null || size != null) {
            return resolved;
        }
        return DEFAULT_SIZE;
    }

    private boolean matchesQuery(CompanyDTO company, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String normalized = normalize(query);
        return contains(company.name(), normalized)
            || contains(company.industry(), normalized)
            || contains(company.location(), normalized)
            || contains(company.website(), normalized)
            || contains(company.description(), normalized);
    }

    private boolean matchesContains(String value, String filter) {
        if (filter == null || filter.isBlank()) {
            return true;
        }
        return contains(value, normalize(filter));
    }

    private boolean matchesScoreRange(CompanyDTO company, Double minScore, Double maxScore) {
        if (minScore == null && maxScore == null) {
            return true;
        }
        if (company.score() == null) {
            return false;
        }
        int scoreValue = company.score().value();
        if (minScore != null && scoreValue < minScore) {
            return false;
        }
        return maxScore == null || scoreValue <= maxScore;
    }

    private boolean matchesHasContact(CompanyDTO company, Boolean hasContact) {
        if (hasContact == null) {
            return true;
        }
        boolean contactPresent = (company.primaryContactEmail() != null && !company.primaryContactEmail().isBlank())
            || company.contactCount() > 0;
        return hasContact == contactPresent;
    }

    private boolean contains(String value, String normalizedFilter) {
        return value != null && normalize(value).contains(normalizedFilter);
    }

    private String normalize(String value) {
        return value.toLowerCase(Locale.ROOT).trim();
    }
}
