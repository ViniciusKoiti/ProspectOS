package dev.prospectos.infrastructure.service.inmemory;

import java.util.List;

import dev.prospectos.api.dto.ICPDto;
import dev.prospectos.core.domain.ICP;

final class InMemoryLeadIcpResolver {

    private InMemoryLeadIcpResolver() {
    }

    static Long resolveIcpId(Long requestIcpId, Long defaultIcpId) {
        if (requestIcpId != null) {
            return requestIcpId;
        }
        if (defaultIcpId == null) {
            throw new IllegalArgumentException(
                "ICP ID is required. Provide icpId in request or configure prospectos.leads.default-icp-id"
            );
        }
        return defaultIcpId;
    }

    static ICP toDomain(ICPDto icpDto) {
        return ICP.create(
            icpDto.name(),
            icpDto.description(),
            icpDto.targetIndustries() != null ? icpDto.targetIndustries() : List.of(),
            icpDto.regions() != null ? icpDto.regions() : List.of(),
            icpDto.targetRoles() != null ? icpDto.targetRoles() : List.of(),
            icpDto.interestTheme()
        );
    }
}
