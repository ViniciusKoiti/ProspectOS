package dev.prospectos.core.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExternalIdPolicyTest {

    @Test
    void fromUuidGeneratesPositiveJavaScriptSafeId() {
        long id = ExternalIdPolicy.fromUuid(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));

        assertTrue(id > 0);
        assertTrue(id <= ExternalIdPolicy.MAX_SAFE_JS_INTEGER);
    }

    @Test
    void requireSafeRejectsUnsafeValues() {
        assertThrows(IllegalArgumentException.class, () -> ExternalIdPolicy.requireSafe(0L, "id"));
        assertThrows(
            IllegalArgumentException.class,
            () -> ExternalIdPolicy.requireSafe(ExternalIdPolicy.MAX_SAFE_JS_INTEGER + 1, "id")
        );
    }
}
