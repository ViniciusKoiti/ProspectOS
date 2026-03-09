package dev.prospectos.ai.service;

import dev.prospectos.ai.dto.PriorityLevel;
import dev.prospectos.core.domain.Company;

final class MockScoringNarrativeBuilder {

    String reasoning(Company company, int score, PriorityLevel priority) {
        StringBuilder reasoning = new StringBuilder().append(priority.name()).append(" PRIORITY: ");
        if (company.getIndustry() != null) {
            String industry = company.getIndustry().toLowerCase();
            if (industry.contains("fintech") || industry.contains("tech")) {
                reasoning.append("Strong tech industry fit. ");
            } else if (industry.contains("agribusiness")) {
                reasoning.append("Traditional sector with modernization potential. ");
            } else {
                reasoning.append("Industry analysis shows good potential. ");
            }
        }
        if (score >= 80) {
            reasoning.append("High ICP alignment, immediate outreach recommended.");
        } else if (score >= 65) {
            reasoning.append("Good potential, schedule follow-up within 1-2 weeks.");
        } else {
            reasoning.append("Moderate fit, consider for nurture campaigns.");
        }
        if (company.getLocation() != null && company.getLocation().contains("SÃ£o Paulo")) {
            reasoning.append(" Located in Brazil's tech hub.");
        }
        return reasoning.toString();
    }

    String recommendation(PriorityLevel priority) {
        return switch (priority) {
            case HOT -> "Prioritize immediate outreach. Strong alignment with ICP criteria.";
            case WARM -> "Schedule follow-up within 2 weeks. Good potential for conversion.";
            case COLD -> "Add to nurture campaign. Monitor for engagement signals.";
            case IGNORE -> "Low priority. Consider for future re-evaluation.";
        };
    }
}
