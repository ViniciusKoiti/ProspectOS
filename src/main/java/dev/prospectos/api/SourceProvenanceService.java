package dev.prospectos.api;

import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.SourceProvenanceDTO;

public interface SourceProvenanceService {

    void record(CompanyDTO company, SourceProvenanceDTO provenance);

    void record(Long companyExternalId, SourceProvenanceDTO provenance);
}
