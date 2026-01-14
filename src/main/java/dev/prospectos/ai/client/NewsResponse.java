package dev.prospectos.ai.client;

import java.util.List;

/**
 * Response from news search operation.
 */
public record NewsResponse(
    List<String> news
) {
}
