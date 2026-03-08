package dev.prospectos.infrastructure.service.jpa;

import dev.prospectos.api.dto.request.CompanyCreateRequest;
import dev.prospectos.api.dto.request.CompanyUpdateRequest;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.CompanySize;
import dev.prospectos.core.domain.Website;

final class CompanyJpaProfileUpdater {

    Company create(CompanyCreateRequest request) {
        Company company = Company.create(request.name(), Website.of(request.website()), request.industry());
        applyOptionalFields(company, request.description(), request.size(), request.country(), request.city());
        return company;
    }

    void update(Company company, CompanyUpdateRequest request) {
        company.updateProfile(request.name(), Website.of(request.website()), request.industry());
        applyOptionalFields(company, request.description(), request.size(), request.country(), request.city());
    }

    private void applyOptionalFields(Company company, String description, String size, String country, String city) {
        if (description != null) {
            company.setDescription(description);
        }
        if (size != null) {
            company.setSize(parseCompanySize(size));
        }
        if (country != null || city != null) {
            company.setLocation(country, city);
        }
    }

    private CompanySize parseCompanySize(String size) {
        if (size == null || size.trim().isEmpty()) {
            return null;
        }
        try {
            return CompanySize.valueOf(size.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid company size: " + size);
        }
    }
}

