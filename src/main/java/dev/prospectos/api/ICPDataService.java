package dev.prospectos.api;

import dev.prospectos.api.dto.ICPDto;

/**
 * Public interface for ICP data access across modules.
 */
public interface ICPDataService {

    ICPDto findICP(Long icpId);
}
