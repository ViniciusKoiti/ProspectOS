package dev.prospectos.infrastructure.service.jpa;

import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.Score;

final class CompanyJpaDtoMapper {

    CompanyDTO toDTO(Company company) {
        ScoreDTO scoreDTO = toScoreDTO(company.getProspectingScore());
        return new CompanyDTO(
            company.getId().getMostSignificantBits(),
            company.getName(),
            company.getIndustry(),
            company.getWebsite() != null ? company.getWebsite().getUrl() : null,
            company.getDescription(),
            null,
            company.getLocation(),
            scoreDTO
        );
    }

    private ScoreDTO toScoreDTO(Score score) {
        if (score == null) {
            return null;
        }
        return new ScoreDTO(score.getValue().intValue(), toPriority(score), "Score from database");
    }

    private String toPriority(Score score) {
        if (score == null) {
            return "COLD";
        }
        return switch (score.getCategory()) {
            case HIGH -> "HOT";
            case MEDIUM -> "WARM";
            case LOW, VERY_LOW -> "COLD";
        };
    }
}
