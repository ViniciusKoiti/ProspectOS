package dev.prospectos.core.util;

import dev.prospectos.core.domain.Website;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Generates deterministic lead keys for idempotency in the lead preview/accept flow.
 * <p>
 * Lead keys are based on the combination of company domain and source name,
 * ensuring that the same lead from the same source always generates the same key.
 */
public final class LeadKeyGenerator {

    private LeadKeyGenerator() {
        // utility class
    }

    /**
     * Generates a lead key from a website URL and source name.
     *
     * @param websiteUrl the company website URL
     * @param sourceName the source that provided this lead (e.g., "in-memory", "apollo")
     * @return a URL-safe base64-encoded SHA-256 hash
     * @throws IllegalArgumentException if websiteUrl is null or invalid
     */
    public static String generate(String websiteUrl, String sourceName) {
        if (websiteUrl == null || websiteUrl.isBlank()) {
            throw new IllegalArgumentException("Website URL cannot be null or blank");
        }
        if (sourceName == null || sourceName.isBlank()) {
            throw new IllegalArgumentException("Source name cannot be null or blank");
        }

        Website website = Website.of(websiteUrl);
        String domain = website.getDomain();
        String normalizedSource = sourceName.toLowerCase().trim();

        String input = domain + "|" + normalizedSource;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is always available in Java 21
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Validates that a lead key matches the expected format.
     *
     * @param leadKey the key to validate
     * @return true if the key appears to be a valid SHA-256 base64url hash
     */
    public static boolean isValid(String leadKey) {
        if (leadKey == null || leadKey.isBlank()) {
            return false;
        }
        // SHA-256 produces 32 bytes, base64url without padding is 43 chars
        return leadKey.length() == 43 && leadKey.matches("[A-Za-z0-9_-]+");
    }
}
