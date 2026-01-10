package dev.prospectos.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SourceProvenanceRepository extends JpaRepository<SourceProvenance, UUID> {

    List<SourceProvenance> findByCompanyExternalId(Long companyExternalId);
}
