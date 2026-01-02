package dev.prospectos.infrastructure.service.jpa;

import dev.prospectos.api.ICPDataService;
import dev.prospectos.api.dto.ICPDto;
import dev.prospectos.api.dto.request.ICPCreateRequest;
import dev.prospectos.api.dto.request.ICPUpdateRequest;
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

    @Override
    public List<ICPDto> findAllICPs() {
        return icpRepository.findAll()
            .stream()
            .map(this::toDTO)
            .toList();
    }

    @Override
    public ICPDto createICP(ICPCreateRequest request) {
        ICP icp = ICP.create(
            request.name(),
            request.description(),
            request.industries(),
            request.regions(),
            request.targetRoles(),
            request.interestTheme()
        );
        return toDTO(icpRepository.save(icp));
    }

    @Override
    public ICPDto updateICP(Long icpId, ICPUpdateRequest request) {
        Optional<ICP> existing = findICPByExternalId(icpId);
        if (existing.isEmpty()) {
            return null;
        }
        ICP icp = existing.get();
        icp.updateProfile(
            request.name(),
            request.description(),
            request.industries(),
            request.regions(),
            request.targetRoles(),
            request.interestTheme()
        );
        return toDTO(icpRepository.save(icp));
    }

    @Override
    public boolean deleteICP(Long icpId) {
        Optional<ICP> existing = findICPByExternalId(icpId);
        if (existing.isEmpty()) {
            return false;
        }
        icpRepository.delete(existing.get());
        return true;
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
