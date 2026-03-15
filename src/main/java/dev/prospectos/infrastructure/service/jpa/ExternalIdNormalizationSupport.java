package dev.prospectos.infrastructure.service.jpa;

import dev.prospectos.core.domain.ExternalIdPolicy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class ExternalIdNormalizationSupport {

    private ExternalIdNormalizationSupport() {
    }

    static Set<Long> collectSafeIds(List<Long> ids) {
        Set<Long> used = new HashSet<>();
        for (Long id : ids) {
            if (ExternalIdPolicy.isSafe(id)) {
                used.add(id);
            }
        }
        return used;
    }

    static long nextStart(Set<Long> usedIds) {
        long maxUsed = usedIds.stream().mapToLong(Long::longValue).max().orElse(0L);
        return Math.max(1L, maxUsed + 1L);
    }

    static long nextAvailable(Set<Long> usedIds, long start) {
        long candidate = Math.max(1L, start);
        while (candidate <= ExternalIdPolicy.MAX_SAFE_JS_INTEGER && usedIds.contains(candidate)) {
            candidate++;
        }
        if (candidate > ExternalIdPolicy.MAX_SAFE_JS_INTEGER) {
            throw new IllegalStateException("No JavaScript-safe external IDs available");
        }
        return candidate;
    }
}
