package dev.prospectos.infrastructure.service.inmemory;

import dev.prospectos.api.dto.ICPDto;
import dev.prospectos.api.dto.request.ICPCreateRequest;
import dev.prospectos.api.dto.request.ICPUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryICPDataServiceTest {

    private InMemoryICPDataService service;

    @BeforeEach
    void setUp() {
        service = new InMemoryICPDataService(new InMemoryCoreDataStore());
    }

    @Test
    void createIcpMapsNullCollectionsToEmptyLists() {
        ICPDto created = service.createICP(new ICPCreateRequest(
            "ICP Null",
            "desc",
            null,
            null,
            null,
            "theme",
            null,
            null,
            null
        ));

        assertNotNull(created.id());
        assertTrue(created.targetIndustries().isEmpty());
        assertTrue(created.regions().isEmpty());
        assertTrue(created.targetRoles().isEmpty());
        assertTrue(created.targetTechnologies().isEmpty());
        assertEquals(created, service.findICP(created.id()));
    }

    @Test
    void createIcpKeepsProvidedCollectionsAndRanges() {
        ICPDto created = service.createICP(new ICPCreateRequest(
            "ICP Full",
            "desc",
            List.of("SaaS"),
            List.of("BR"),
            List.of("CTO"),
            "growth",
            List.of("AWS"),
            50,
            500
        ));

        assertEquals(List.of("SaaS"), created.targetIndustries());
        assertEquals(List.of("BR"), created.regions());
        assertEquals(List.of("CTO"), created.targetRoles());
        assertEquals(List.of("AWS"), created.targetTechnologies());
        assertEquals(50, created.minEmployeeCount());
        assertEquals(500, created.maxEmployeeCount());
    }

    @Test
    void updateReturnsNullWhenIcpDoesNotExist() {
        ICPDto updated = service.updateICP(999L, new ICPUpdateRequest(
            "Missing",
            "desc",
            List.of("X"),
            List.of("Y"),
            List.of("Z"),
            "theme",
            List.of("A"),
            1,
            10
        ));

        assertNull(updated);
    }

    @Test
    void updateReplacesDataAndMapsNullCollectionsToEmpty() {
        ICPDto updated = service.updateICP(1L, new ICPUpdateRequest(
            "Updated",
            "updated desc",
            null,
            null,
            null,
            "new theme",
            null,
            10,
            100
        ));

        assertNotNull(updated);
        assertEquals(1L, updated.id());
        assertEquals("Updated", updated.name());
        assertTrue(updated.targetIndustries().isEmpty());
        assertTrue(updated.regions().isEmpty());
        assertTrue(updated.targetRoles().isEmpty());
        assertTrue(updated.targetTechnologies().isEmpty());
        assertEquals(10, updated.minEmployeeCount());
        assertEquals(100, updated.maxEmployeeCount());
    }

    @Test
    void findAllReturnsCopyOfCurrentIcps() {
        List<ICPDto> all = service.findAllICPs();

        assertFalse(all.isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> all.add(all.get(0)));
    }

    @Test
    void deleteReturnsTrueForExistingAndFalseForMissing() {
        ICPDto created = service.createICP(new ICPCreateRequest(
            "To Delete",
            "desc",
            List.of(),
            List.of(),
            List.of(),
            "theme",
            List.of(),
            null,
            null
        ));

        assertTrue(service.deleteICP(created.id()));
        assertNull(service.findICP(created.id()));
        assertFalse(service.deleteICP(created.id()));
    }
}
