package dev.prospectos.infrastructure.service.leads;

import dev.prospectos.api.ICPDataService;
import dev.prospectos.api.dto.ICPDto;
import dev.prospectos.core.domain.ICP;

final class ScraperLeadIcpLoader {

    private final ICPDataService icpDataService;

    ScraperLeadIcpLoader(ICPDataService icpDataService) {
        this.icpDataService = icpDataService;
    }

    ICP load(Long icpId) {
        ICPDto icpDto = icpDataService.findICP(icpId);
        if (icpDto == null) {
            throw new IllegalArgumentException("ICP not found with id: " + icpId);
        }
        return ScraperLeadIcpMapper.toDomainICP(icpDto);
    }
}
