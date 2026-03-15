package dev.prospectos.core.domain;

import java.util.UUID;

/**
 * Defines a JavaScript-safe policy for external IDs exposed via REST APIs.
 */
public final class ExternalIdPolicy {

    public static final long MAX_SAFE_JS_INTEGER = 9_007_199_254_740_991L;

    private ExternalIdPolicy() {
    }

    public static long fromUuid(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("External ID source cannot be null");
        }
        long positiveBits = id.getMostSignificantBits() & Long.MAX_VALUE;
        long candidate = positiveBits % MAX_SAFE_JS_INTEGER;
        return candidate == 0 ? 1 : candidate;
    }

    public static boolean isSafe(Long externalId) {
        return externalId != null && externalId > 0 && externalId <= MAX_SAFE_JS_INTEGER;
    }

    public static long requireSafe(Long externalId, String label) {
        if (!isSafe(externalId)) {
            throw new IllegalArgumentException(label + " must be between 1 and " + MAX_SAFE_JS_INTEGER);
        }
        return externalId;
    }
}
