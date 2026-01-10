package dev.prospectos.infrastructure.service.inmemory;

import dev.prospectos.api.ICPDataService;
import dev.prospectos.api.dto.ICPDto;
import dev.prospectos.api.dto.request.ICPCreateRequest;
import dev.prospectos.api.dto.request.ICPUpdateRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * In-memory ICP data service for demo and test profiles.
 */
@Service
@Profile({"demo", "test", "mock"})
public class InMemoryICPDataService implements ICPDataService {

    private final InMemoryCoreDataStore store;

    public InMemoryICPDataService(InMemoryCoreDataStore store) {
        this.store = store;
    }

    @Override
    public ICPDto findICP(Long icpId) {
        return store.icps().get(icpId);
    }

    @Override
    public List<ICPDto> findAllICPs() {
        return List.copyOf(store.icps().values());
    }

    @Override
    public ICPDto createICP(ICPCreateRequest request) {
        long icpId = store.nextIcpId();
        ICPDto icp = new ICPDto(
            icpId,
            request.name(),
            request.description(),
            request.industries(),
            List.of(),
            null,
            null,
            request.targetRoles()
        );
        store.icps().put(icpId, icp);
        return icp;
    }

    @Override
    public ICPDto updateICP(Long icpId, ICPUpdateRequest request) {
        if (!store.icps().containsKey(icpId)) {
            return null;
        }
        ICPDto icp = new ICPDto(
            icpId,
            request.name(),
            request.description(),
            request.industries(),
            List.of(),
            null,
            null,
            request.targetRoles()
        );
        store.icps().put(icpId, icp);
        return icp;
    }

    @Override
    public boolean deleteICP(Long icpId) {
        return store.icps().remove(icpId) != null;
    }
}
