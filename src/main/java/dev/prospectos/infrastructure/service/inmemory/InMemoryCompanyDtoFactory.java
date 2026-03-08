package dev.prospectos.infrastructure.service.inmemory;

import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.api.dto.request.CompanyCreateRequest;
import dev.prospectos.api.dto.request.CompanyUpdateRequest;

final class InMemoryCompanyDtoFactory {

    CompanyDTO fromCreateRequest(long companyId, CompanyCreateRequest request) {
        return new CompanyDTO(
            companyId,
            request.name(),
            request.industry(),
            request.website(),
            request.description(),
            null,
            formatLocation(request.country(), request.city()),
            null
        );
    }

    CompanyDTO fromUpdateRequest(Long companyId, CompanyUpdateRequest request, ScoreDTO existingScore) {
        return new CompanyDTO(
            companyId,
            request.name(),
            request.industry(),
            request.website(),
            request.description(),
            null,
            formatLocation(request.country(), request.city()),
            existingScore
        );
    }

    CompanyDTO withScore(CompanyDTO existing, ScoreDTO score) {
        return new CompanyDTO(
            existing.id(),
            existing.name(),
            existing.industry(),
            existing.website(),
            existing.description(),
            existing.employeeCount(),
            existing.location(),
            score
        );
    }

    private String formatLocation(String country, String city) {
        if (city != null && country != null) {
            return city + ", " + country;
        }
        if (city != null) {
            return city;
        }
        return country;
    }
}
