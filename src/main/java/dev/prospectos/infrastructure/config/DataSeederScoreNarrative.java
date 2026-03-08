package dev.prospectos.infrastructure.config;

import dev.prospectos.api.dto.CompanyDTO;

final class DataSeederScoreNarrative {

    String reasoning(CompanyDTO company, int score) {
        StringBuilder value = new StringBuilder(priorityLabel(score));
        value.append(industryReasoning(company));
        value.append(sizeReasoning(company));
        if (company.location() != null && company.location().contains("São Paulo")) {
            value.append(" Located in Brazil's tech hub.");
        }
        return value.toString();
    }

    String categorize(int score) {
        if (score >= 80) {
            return "HOT";
        }
        if (score >= 65) {
            return "WARM";
        }
        return "COLD";
    }

    private String priorityLabel(int score) {
        if (score >= 80) {
            return "HIGH PRIORITY: ";
        }
        if (score >= 65) {
            return "MEDIUM PRIORITY: ";
        }
        return "LOW PRIORITY: ";
    }

    private String industryReasoning(CompanyDTO company) {
        return switch (company.industry()) {
            case "fintech", "technology", "saas" -> "Strong tech industry fit. ";
            case "agtech" -> "Growing AgTech sector with digital adoption. ";
            case "agribusiness" -> "Traditional agro with modernization potential. ";
            case "consulting" -> "Service industry with technology needs. ";
            default -> "Industry analysis complete. ";
        };
    }

    private String sizeReasoning(CompanyDTO company) {
        if (company.name().toLowerCase().contains("startup") || (company.employeeCount() != null && company.employeeCount() < 50)) {
            return "High growth potential, early-stage innovation.";
        }
        if (company.employeeCount() != null && company.employeeCount() < 200) {
            return "Agile company, good decision-making speed.";
        }
        if (company.employeeCount() != null && company.employeeCount() < 1000) {
            return "Established operations with growth capacity.";
        }
        return "Stable enterprise, complex decision process.";
    }
}
