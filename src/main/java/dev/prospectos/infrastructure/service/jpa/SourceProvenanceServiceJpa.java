package dev.prospectos.infrastructure.service.jpa;

import dev.prospectos.api.SourceProvenanceService;
import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.SourceProvenanceDTO;
import dev.prospectos.infrastructure.jpa.SourceProvenance;
import dev.prospectos.infrastructure.jpa.SourceProvenanceRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class SourceProvenanceServiceJpa implements SourceProvenanceService {

    private final SourceProvenanceRepository sourceProvenanceRepository;

    public SourceProvenanceServiceJpa(SourceProvenanceRepository sourceProvenanceRepository) {
        this.sourceProvenanceRepository = sourceProvenanceRepository;
    }

    @Override
    public void record(CompanyDTO company, SourceProvenanceDTO provenance) {
        if (company == null || provenance == null) {
            return;
        }
        record(company.id(), provenance);
    }

    @Override
    public void record(Long companyExternalId, SourceProvenanceDTO provenance) {
        if (companyExternalId == null || provenance == null) {
            return;
        }

        String sourceName = provenance.sourceName();
        String sourceUrl = provenance.sourceUrl();
        Instant capturedAt = provenance.collectedAt() != null ? provenance.collectedAt() : Instant.now();

        SourceProvenance record = SourceProvenance.of(companyExternalId, sourceName, sourceUrl, capturedAt);
        sourceProvenanceRepository.save(record);
    }
}
