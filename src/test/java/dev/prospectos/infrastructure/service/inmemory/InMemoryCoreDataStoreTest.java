package dev.prospectos.infrastructure.service.inmemory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryCoreDataStoreTest {

    @Test
    void seedsCompaniesIcpAndRelationshipDataOnStartup() {
        InMemoryCoreDataStore store = new InMemoryCoreDataStore();

        assertEquals(7, store.companies().size());
        assertEquals(1, store.icps().size());
        assertTrue(store.icpCompanies().containsKey(1L));
        assertEquals(7, store.icpCompanies().get(1L).size());
        assertTrue(store.companyScores().isEmpty());
    }

    @Test
    void nextIdsStartAfterSeededData() {
        InMemoryCoreDataStore store = new InMemoryCoreDataStore();

        assertEquals(8L, store.nextCompanyId());
        assertEquals(2L, store.nextIcpId());
    }
}
