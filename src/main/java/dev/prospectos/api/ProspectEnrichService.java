package dev.prospectos.api;

import dev.prospectos.core.domain.Company;

/**
 * Port for enriching company data through AI analysis.
 */
public interface ProspectEnrichService {

    String enrichCompany(Company company);
}
