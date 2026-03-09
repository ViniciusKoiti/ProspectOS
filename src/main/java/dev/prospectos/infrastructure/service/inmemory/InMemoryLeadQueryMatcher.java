package dev.prospectos.infrastructure.service.inmemory;

import java.util.List;
import java.util.Locale;

import dev.prospectos.api.dto.CompanyDTO;

final class InMemoryLeadQueryMatcher {

    private InMemoryLeadQueryMatcher() {
    }

    static List<String> tokens(String query) {
        return List.of(query.toLowerCase(Locale.ROOT).split("\\s+"));
    }

    static boolean matches(CompanyDTO company, List<String> tokens) {
        String haystack = String.join(
            " ",
            safe(company.name()),
            safe(company.industry()),
            safe(company.location()),
            safe(company.description())
        ).toLowerCase(Locale.ROOT);

        for (String token : tokens) {
            if (!token.isBlank() && haystack.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
