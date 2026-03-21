package dev.prospectos.infrastructure.api.leads;

import dev.prospectos.api.LeadSearchAsyncService;
import dev.prospectos.api.dto.LeadSearchAsyncSnapshotResponse;
import dev.prospectos.api.dto.LeadSearchAsyncStartResponse;
import dev.prospectos.api.dto.LeadSearchRequest;
import dev.prospectos.infrastructure.service.leads.LeadSearchAsyncStreamService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Async lead search endpoints (start, status, and SSE stream).
 */
@RestController
@RequestMapping("/api/leads")
public class LeadSearchAsyncController {

    private final LeadSearchAsyncService leadSearchAsyncService;
    private final LeadSearchAsyncStreamService streamService;

    public LeadSearchAsyncController(
        LeadSearchAsyncService leadSearchAsyncService,
        LeadSearchAsyncStreamService streamService
    ) {
        this.leadSearchAsyncService = leadSearchAsyncService;
        this.streamService = streamService;
    }

    @PostMapping("/search/async")
    public ResponseEntity<LeadSearchAsyncStartResponse> startSearch(@Valid @RequestBody LeadSearchRequest request) {
        LeadSearchAsyncStartResponse response = leadSearchAsyncService.startSearch(request);
        return ResponseEntity.accepted().body(response);
    }

    @GetMapping("/search/{requestId}")
    public ResponseEntity<LeadSearchAsyncSnapshotResponse> getStatus(@PathVariable UUID requestId) {
        LeadSearchAsyncSnapshotResponse snapshot = leadSearchAsyncService.getSnapshot(requestId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Search request not found: " + requestId));
        return ResponseEntity.ok(snapshot);
    }

    @GetMapping(path = "/search/{requestId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable UUID requestId) {
        if (leadSearchAsyncService.getSnapshot(requestId).isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "Search request not found: " + requestId);
        }
        return streamService.subscribe(requestId);
    }
}
