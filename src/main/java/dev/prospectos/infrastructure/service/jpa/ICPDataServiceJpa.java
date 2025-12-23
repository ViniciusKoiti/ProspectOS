package dev.prospectos.infrastructure.service.jpa;

import dev.prospectos.api.ICPDataService;
import dev.prospectos.api.dto.ICPDto;
import dev.prospectos.core.domain.ICP;
import dev.prospectos.core.repository.ICPDomainRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository-backed ICP data service for non-test profiles.
 */
@Service
@Profile("!demo & !test")
public class ICPDataServiceJpa implements ICPDataService {

    private final ICPDomainRepository icpRepository;

    public ICPDataServiceJpa(ICPDomainRepository icpRepository) {
        this.icpRepository = icpRepository;
    }

    @Override
    public ICPDto findICP(Long icpId) {
        return findICPByExternalId(icpId)
            .map(this::toDTO)
            .orElse(null);
    }

    private Optional<ICP> findICPByExternalId(Long externalId) {
        if (externalId == null) {
            return Optional.empty();
        }
        return icpRepository.findAll()
            .stream()
            .filter(icp -> matchesExternalId(icp.getId(), externalId))
            .findFirst();
    }

    private boolean matchesExternalId(UUID id, Long externalId) {
        return id != null && id.getMostSignificantBits() == externalId;
    }

    private ICPDto toDTO(ICP icp) {
        return new ICPDto(
            icp.getId().getMostSignificantBits(),
            icp.getName(),
            icp.getDescription(),
            icp.getIndustries(),
            List.of(),
            null,
            null,
            icp.getTargetRoles()
        );
    }
}
