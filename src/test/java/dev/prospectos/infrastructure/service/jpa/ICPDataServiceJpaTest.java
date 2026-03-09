package dev.prospectos.infrastructure.service.jpa;

import dev.prospectos.api.dto.ICPDto;
import dev.prospectos.api.dto.request.ICPCreateRequest;
import dev.prospectos.api.dto.request.ICPUpdateRequest;
import dev.prospectos.core.domain.ICP;
import dev.prospectos.core.repository.ICPDomainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ICPDataServiceJpaTest {

    @Mock
    private ICPDomainRepository icpRepository;

    private ICPDataServiceJpa service;

    @BeforeEach
    void setUp() {
        service = new ICPDataServiceJpa(icpRepository);
    }

    @Test
    void createIcpMapsNullCollectionsToEmptyLists() {
        ICPCreateRequest request = new ICPCreateRequest("ICP", "desc", null, null, null, "theme", null, null, null);
        when(icpRepository.save(org.mockito.ArgumentMatchers.any(ICP.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        ICPDto created = service.createICP(request);

        ArgumentCaptor<ICP> captor = ArgumentCaptor.forClass(ICP.class);
        verify(icpRepository).save(captor.capture());
        ICP saved = captor.getValue();

        assertNotNull(created.id());
        assertTrue(saved.getIndustries().isEmpty());
        assertTrue(saved.getRegions().isEmpty());
        assertTrue(saved.getTargetRoles().isEmpty());
    }

    @Test
    void findAndUpdateReturnNullWhenIcpIsMissing() {
        ICPUpdateRequest updateRequest = new ICPUpdateRequest("ICP", "desc", List.of(), List.of(), List.of(), "theme", null, null, null);
        when(icpRepository.findByExternalId(99L)).thenReturn(Optional.empty());

        assertNull(service.findICP(99L));
        assertNull(service.updateICP(99L, updateRequest));
    }

    @Test
    void deleteReturnsFalseWhenIcpIsMissing() {
        when(icpRepository.findByExternalId(42L)).thenReturn(Optional.empty());

        assertFalse(service.deleteICP(42L));
    }

    @Test
    void findAllMapsExternalIdAndBasicFields() {
        ICP icp = ICP.createWithExternalId(7L, "ICP 7", "desc", List.of("Tech"), List.of("BR"), List.of("CTO"), "theme");
        when(icpRepository.findAll()).thenReturn(List.of(icp));

        List<ICPDto> found = service.findAllICPs();

        assertEquals(1, found.size());
        assertEquals(7L, found.get(0).id());
        assertEquals("ICP 7", found.get(0).name());
    }
}
