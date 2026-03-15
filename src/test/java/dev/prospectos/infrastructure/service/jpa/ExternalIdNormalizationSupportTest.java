package dev.prospectos.infrastructure.service.jpa;

import dev.prospectos.core.domain.ExternalIdPolicy;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExternalIdNormalizationSupportTest {

    @Test
    void collectSafeIdsKeepsOnlyJavaScriptSafePositiveValues() {
        Set<Long> safeIds = ExternalIdNormalizationSupport.collectSafeIds(Arrays.asList(
            1L,
            10L,
            0L,
            -1L,
            null,
            ExternalIdPolicy.MAX_SAFE_JS_INTEGER,
            ExternalIdPolicy.MAX_SAFE_JS_INTEGER + 1
        ));

        assertEquals(Set.of(1L, 10L, ExternalIdPolicy.MAX_SAFE_JS_INTEGER), safeIds);
    }

    @Test
    void nextStartReturnsOneWhenNoIdsAndMaxPlusOneOtherwise() {
        assertEquals(1L, ExternalIdNormalizationSupport.nextStart(Set.of()));
        assertEquals(42L, ExternalIdNormalizationSupport.nextStart(Set.of(1L, 20L, 41L)));
    }

    @Test
    void nextAvailableStartsAtOneSkipsUsedIdsAndReturnsCandidate() {
        Set<Long> used = Set.of(1L, 2L, 3L, 8L);

        assertEquals(4L, ExternalIdNormalizationSupport.nextAvailable(used, 0L));
        assertEquals(9L, ExternalIdNormalizationSupport.nextAvailable(used, 8L));
    }

    @Test
    void nextAvailableThrowsWhenNoSafeIdIsLeft() {
        Set<Long> used = Set.of(ExternalIdPolicy.MAX_SAFE_JS_INTEGER);

        assertThrows(
            IllegalStateException.class,
            () -> ExternalIdNormalizationSupport.nextAvailable(used, ExternalIdPolicy.MAX_SAFE_JS_INTEGER)
        );
    }
}