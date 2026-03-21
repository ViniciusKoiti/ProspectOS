package dev.prospectos.api;

import dev.prospectos.api.dto.LeadSearchAsyncSnapshotResponse;
import dev.prospectos.api.dto.LeadSearchAsyncStartResponse;
import dev.prospectos.api.dto.LeadSearchRequest;

import java.util.Optional;
import java.util.UUID;

/**
 * Async lead search contract for job-based execution.
 */
public interface LeadSearchAsyncService {

    LeadSearchAsyncStartResponse startSearch(LeadSearchRequest request);

    Optional<LeadSearchAsyncSnapshotResponse> getSnapshot(UUID requestId);
}
