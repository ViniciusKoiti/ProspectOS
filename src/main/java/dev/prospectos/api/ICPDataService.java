package dev.prospectos.api;

import dev.prospectos.api.dto.ICPDto;
import dev.prospectos.api.dto.request.ICPCreateRequest;
import dev.prospectos.api.dto.request.ICPUpdateRequest;
import java.util.List;

/**
 * Public interface for ICP data access across modules.
 */
public interface ICPDataService {

    ICPDto findICP(Long icpId);

    List<ICPDto> findAllICPs();

    ICPDto createICP(ICPCreateRequest request);

    ICPDto updateICP(Long icpId, ICPUpdateRequest request);

    boolean deleteICP(Long icpId);
}
