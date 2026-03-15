package dev.prospectos.infrastructure.service.jpa;

import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.Contact;
import dev.prospectos.core.domain.Email;
import dev.prospectos.core.domain.Score;

import java.util.Objects;

final class CompanyJpaDtoMapper {

    CompanyDTO toDTO(Company company) {
        ScoreDTO scoreDTO = toScoreDTO(company.getProspectingScore());
        String primaryContactEmail = findPrimaryContactEmail(company);
        int contactCount = company.getContacts().size();
        return new CompanyDTO(
            company.getExternalId(),
            company.getName(),
            company.getIndustry(),
            company.getWebsite() != null ? company.getWebsite().getUrl() : null,
            company.getDescription(),
            null,
            company.getLocation(),
            scoreDTO,
            primaryContactEmail,
            contactCount
        );
    }

    private String findPrimaryContactEmail(Company company) {
        return company.getContacts().stream()
            .map(Contact::getEmail)
            .filter(Objects::nonNull)
            .map(Email::getAddress)
            .findFirst()
            .orElse(null);
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
