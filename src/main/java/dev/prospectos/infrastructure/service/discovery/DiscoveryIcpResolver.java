package dev.prospectos.infrastructure.service.discovery;

import java.util.List;

import dev.prospectos.api.dto.ICPDto;
import dev.prospectos.core.domain.ICP;

final class DiscoveryIcpResolver {

    private DiscoveryIcpResolver() {
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
            icpDto.targetIndustries() == null ? List.of() : icpDto.targetIndustries(),
            icpDto.regions() == null ? List.of() : icpDto.regions(),
            icpDto.targetRoles() == null ? List.of() : icpDto.targetRoles(),
            icpDto.interestTheme()
        );
    }
}
