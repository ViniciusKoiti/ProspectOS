package dev.prospectos.api.mapper;

import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.Website;

/**
 * Mapper utility to convert between CompanyDTO and Company domain objects.
 * Consolidates duplicate toDomainCompany methods across the codebase.
 */
public final class CompanyMapper {

    private CompanyMapper() {
        // Utility class - no instantiation
    }

    /**
     * Converts a CompanyDTO to a Company domain object.
     *
     * @param companyDTO the DTO to convert
     * @return the corresponding domain object
     */
    public static Company toDomain(CompanyDTO companyDTO) {
        if (companyDTO == null) {
            return null;
        }

        Company company = Company.create(
            companyDTO.name(),
            Website.of(companyDTO.website()),
            companyDTO.industry()
        );

        if (companyDTO.description() != null && !companyDTO.description().isBlank()) {
            company.setDescription(companyDTO.description().trim());
        }

        return company;
    }
}
