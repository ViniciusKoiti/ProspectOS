package dev.prospectos.core.enrichment;

import java.util.Map;

import dev.prospectos.core.domain.CompanySize;

final class CompanySizeMapper {

    private static final Map<String, CompanySize> SIZE_MAPPINGS = Map.of(
        "startup", CompanySize.STARTUP,
        "small", CompanySize.SMALL,
        "medium", CompanySize.MEDIUM,
        "large", CompanySize.LARGE,
        "enterprise", CompanySize.ENTERPRISE,
        "1-10", CompanySize.STARTUP,
        "11-50", CompanySize.SMALL,
        "51-200", CompanySize.MEDIUM,
        "201-1000", CompanySize.LARGE,
        "1000+", CompanySize.ENTERPRISE
    );

    CompanySize mapCompanySize(String sizeString) {
        if (sizeString == null || sizeString.trim().isEmpty()) {
            return null;
        }
        String normalized = sizeString.trim().toLowerCase();
        CompanySize mapped = SIZE_MAPPINGS.get(normalized);
        if (mapped != null) {
            return mapped;
        }
        String numbersOnly = normalized.replaceAll("[^\\d\\-+]", "");
        if (numbersOnly.contains("-")) {
            String[] parts = numbersOnly.split("-");
            try {
                return mapEmployeeCount(Integer.parseInt(parts[0]));
            } catch (NumberFormatException e) {
                // fall through
            }
        }
        if (numbersOnly.endsWith("+")) {
            return mapEmployeeCount(numbersOnly.substring(0, numbersOnly.length() - 1));
        }
        if (!numbersOnly.isEmpty()) {
            CompanySize byCount = mapEmployeeCount(numbersOnly);
            if (byCount != null) {
                return byCount;
            }
        }
        if (normalized.contains("startup")) {
            return CompanySize.STARTUP;
        }
        if (normalized.contains("enterprise")) {
            return CompanySize.ENTERPRISE;
        }
        if (normalized.contains("large")) {
            return CompanySize.LARGE;
        }
        if (normalized.contains("medium")) {
            return CompanySize.MEDIUM;
        }
        if (normalized.contains("small")) {
            return CompanySize.SMALL;
        }
        return null;
    }

    private CompanySize mapEmployeeCount(String countString) {
        try {
            return mapEmployeeCount(Integer.parseInt(countString));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private CompanySize mapEmployeeCount(int count) {
        if (count <= 10) {
            return CompanySize.STARTUP;
        }
        if (count <= 50) {
            return CompanySize.SMALL;
        }
        if (count <= 200) {
            return CompanySize.MEDIUM;
        }
        if (count <= 1000) {
            return CompanySize.LARGE;
        }
        return CompanySize.ENTERPRISE;
    }
}

