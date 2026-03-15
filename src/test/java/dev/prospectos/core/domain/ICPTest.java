package dev.prospectos.core.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ICPTest {

    @Test
    void createNormalizesNameAndCopiesCollections() {
        List<String> industries = List.of("Tech");
        List<String> regions = List.of("BR");
        List<String> roles = List.of("CTO");

        ICP icp = ICP.create("  SaaS ICP  ", "desc", industries, regions, roles, "ai");

        assertEquals("SaaS ICP", icp.getName());
        assertEquals(List.of("Tech"), icp.getIndustries());
        assertEquals(List.of("BR"), icp.getRegions());
        assertEquals(List.of("CTO"), icp.getTargetRoles());
        assertTrue(ExternalIdPolicy.isSafe(icp.getExternalId()));
    }

    @Test
    void createInitializesEmptyListsWhenNull() {
        ICP icp = ICP.create("Valid", "desc", null, null, null, "theme");

        assertTrue(icp.getIndustries().isEmpty());
        assertTrue(icp.getRegions().isEmpty());
        assertTrue(icp.getTargetRoles().isEmpty());
    }

    @Test
    void createWithExternalIdRejectsOutOfRangeValues() {
        assertThrows(
            IllegalArgumentException.class,
            () -> ICP.createWithExternalId(0L, "Valid", "desc", List.of(), List.of(), List.of(), "theme")
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> ICP.createWithExternalId(
                ExternalIdPolicy.MAX_SAFE_JS_INTEGER + 1,
                "Valid",
                "desc",
                List.of(),
                List.of(),
                List.of(),
                "theme"
            )
        );
    }

    @Test
    void updateProfileRejectsBlankName() {
        ICP icp = ICP.create("Valid", "desc", List.of(), List.of(), List.of(), "theme");

        assertThrows(
            IllegalArgumentException.class,
            () -> icp.updateProfile(" ", "desc", List.of(), List.of(), List.of(), "theme")
        );
    }
}
