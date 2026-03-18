package dev.prospectos.infrastructure.api.companies;

import java.util.List;
import java.util.Locale;

import dev.prospectos.api.dto.CompanyDTO;

final class CompanyListFilter {

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
        CompanyPageWindow pageWindow = CompanyPageWindow.resolve(page, size);

        List<CompanyDTO> filtered = source.stream()
            .filter(company -> matchesQuery(company, query))
            .filter(company -> matchesContains(company.industry(), industry))
            .filter(company -> matchesContains(company.location(), location))
            .filter(company -> matchesScoreRange(company, minScore, maxScore))
            .filter(company -> matchesHasContact(company, hasContact))
            .toList();

        return new Result(pageWindow.apply(filtered), filtered.size());
    }

    private void validateScoreRange(Double minScore, Double maxScore) {
        if (minScore != null && maxScore != null && minScore > maxScore) {
            throw new IllegalArgumentException("minScore cannot be greater than maxScore");
        }
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
