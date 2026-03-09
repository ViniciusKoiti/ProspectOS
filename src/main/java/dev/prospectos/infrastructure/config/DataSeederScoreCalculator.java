package dev.prospectos.infrastructure.config;

import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.ScoreDTO;

final class DataSeederScoreCalculator {

    private final DataSeederScoreNarrative narrative = new DataSeederScoreNarrative();

    ScoreDTO calculateFor(CompanyDTO company) {
        int score = calculateScore(company);
        return new ScoreDTO(score, narrative.categorize(score), narrative.reasoning(company, score));
    }

    private int calculateScore(CompanyDTO company) {
        int baseScore = 60;
        baseScore += industryBonus(company);
        baseScore += sizeBonus(company);
        baseScore += locationBonus(company);
        baseScore += technologyBonus(company);
        baseScore += (int) (Math.random() * 10 - 5);
        return Math.min(Math.max(baseScore, 45), 95);
    }

    private int industryBonus(CompanyDTO company) {
        return switch (company.industry()) {
            case "fintech" -> 15;
            case "technology" -> 12;
            case "agtech", "saas" -> 10;
            case "consulting" -> 7;
            case "agribusiness" -> 5;
            default -> 0;
        };
    }

    private int sizeBonus(CompanyDTO company) {
        if (company.name().toLowerCase().contains("startup") || (company.employeeCount() != null && company.employeeCount() < 50)) {
            return 20;
        }
        if (company.employeeCount() != null && company.employeeCount() < 200) {
            return 15;
        }
        if (company.employeeCount() != null && company.employeeCount() < 1000) {
            return 10;
        }
        return 5;
    }

    private int locationBonus(CompanyDTO company) {
        if (company.location() == null) {
            return 0;
        }
        int bonus = 0;
        if (company.location().contains("São Paulo")) {
            bonus += 10;
        }
        if (company.location().contains("Rio de Janeiro")) {
            bonus += 8;
        }
        if (company.location().contains("Florianópolis") || company.location().contains("Belo Horizonte")) {
            bonus += 6;
        }
        return bonus;
    }

    private int technologyBonus(CompanyDTO company) {
        if (company.name().toLowerCase().contains("tech") || company.description().toLowerCase().contains("digital") || company.description().toLowerCase().contains("platform")) {
            return 8;
        }
        return 0;
    }
}
