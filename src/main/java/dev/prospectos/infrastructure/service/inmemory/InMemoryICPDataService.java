package dev.prospectos.infrastructure.service.inmemory;

import dev.prospectos.api.ICPDataService;
import dev.prospectos.api.dto.ICPDto;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * In-memory ICP data service for demo and test profiles.
 */
@Service
@Profile({"demo", "test"})
public class InMemoryICPDataService implements ICPDataService {

    private final InMemoryCoreDataStore store;

    public InMemoryICPDataService(InMemoryCoreDataStore store) {
        this.store = store;
    }

    @Override
    public ICPDto findICP(Long icpId) {
        return store.icps().get(icpId);
    }
}
