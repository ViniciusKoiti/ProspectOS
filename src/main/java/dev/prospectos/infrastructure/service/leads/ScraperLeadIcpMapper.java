package dev.prospectos.infrastructure.service.leads;

import java.util.List;

import dev.prospectos.api.dto.ICPDto;
import dev.prospectos.core.domain.ICP;

final class ScraperLeadIcpMapper {

    private ScraperLeadIcpMapper() {
    }

    static ICP toDomainICP(ICPDto icpDTO) {
        return ICP.create(
            icpDTO.name(),
            icpDTO.description(),
            icpDTO.targetIndustries() != null ? icpDTO.targetIndustries() : List.of(),
            icpDTO.regions() != null ? icpDTO.regions() : List.of(),
            icpDTO.targetRoles() != null ? icpDTO.targetRoles() : List.of(),
            icpDTO.interestTheme()
        );
    }
}
