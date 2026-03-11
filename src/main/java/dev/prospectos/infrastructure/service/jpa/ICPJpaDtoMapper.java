package dev.prospectos.infrastructure.service.jpa;

import dev.prospectos.api.dto.ICPDto;
import dev.prospectos.core.domain.ICP;

import java.util.List;

final class ICPJpaDtoMapper {

    ICPDto toDTO(ICP icp) {
        return new ICPDto(
            icp.getExternalId(),
            icp.getName(),
            icp.getDescription(),
            copyOrEmpty(icp.getIndustries()),
            copyOrEmpty(icp.getRegions()),
            List.of(),
            null,
            null,
            copyOrEmpty(icp.getTargetRoles()),
            icp.getInterestTheme()
        );
    }

    private List<String> copyOrEmpty(List<String> values) {
        return values == null ? List.of() : List.copyOf(values);
    }
}
