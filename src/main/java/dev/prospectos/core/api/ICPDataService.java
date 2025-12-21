package dev.prospectos.core.api;

import dev.prospectos.core.api.dto.ICPDto;

/**
 * Public interface for ICP data access across modules.
 */
public interface ICPDataService {

    ICPDto findICP(Long icpId);
}
