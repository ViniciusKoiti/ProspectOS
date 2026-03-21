package dev.prospectos.infrastructure.api.leads;

import dev.prospectos.api.LeadSearchAsyncService;
import dev.prospectos.api.dto.LeadSearchAsyncProgress;
import dev.prospectos.api.dto.LeadSearchAsyncSnapshotResponse;
import dev.prospectos.api.dto.LeadSearchAsyncStartResponse;
import dev.prospectos.api.dto.LeadSearchStatus;
import dev.prospectos.infrastructure.service.leads.LeadSearchAsyncStreamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LeadSearchAsyncControllerTest {

    @Mock
    private LeadSearchAsyncService leadSearchAsyncService;

    @Mock
    private LeadSearchAsyncStreamService streamService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new LeadSearchAsyncController(leadSearchAsyncService, streamService))
            .build();
    }

    @Test
    void startSearchReturnsAccepted() throws Exception {
        UUID requestId = UUID.fromString("123e4567-e89b-12d3-a456-426614174011");
        LeadSearchAsyncStartResponse response = new LeadSearchAsyncStartResponse(
            requestId,
            LeadSearchStatus.PROCESSING,
            "Search started",
            Instant.parse("2026-03-21T12:00:00Z")
        );
        when(leadSearchAsyncService.startSearch(any())).thenReturn(response);

        mockMvc.perform(post("/api/leads/search/async")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      \"query\": \"software houses\",
                      \"limit\": 10,
                      \"sources\": [\"in-memory\"],
                      \"icpId\": 1
                    }
                    """))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.requestId").value(requestId.toString()))
            .andExpect(jsonPath("$.status").value("PROCESSING"));
    }

    @Test
    void getStatusReturnsSnapshot() throws Exception {
        UUID requestId = UUID.fromString("123e4567-e89b-12d3-a456-426614174012");
        LeadSearchAsyncSnapshotResponse snapshot = new LeadSearchAsyncSnapshotResponse(
            requestId,
            LeadSearchStatus.PROCESSING,
            "Running",
            new LeadSearchAsyncProgress(1, 2, 0),
            List.of(),
            List.of(),
            Instant.parse("2026-03-21T12:00:00Z"),
            Instant.parse("2026-03-21T12:00:03Z"),
            null
        );
        when(leadSearchAsyncService.getSnapshot(requestId)).thenReturn(Optional.of(snapshot));

        mockMvc.perform(get("/api/leads/search/{requestId}", requestId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PROCESSING"))
            .andExpect(jsonPath("$.progress.doneSources").value(1));
    }

    @Test
    void streamReturnsNotFoundWhenRequestDoesNotExist() throws Exception {
        UUID requestId = UUID.fromString("123e4567-e89b-12d3-a456-426614174013");
        when(leadSearchAsyncService.getSnapshot(requestId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/leads/search/{requestId}/events", requestId))
            .andExpect(status().isNotFound());
    }

    @Test
    void streamReturnsSseWhenRequestExists() throws Exception {
        UUID requestId = UUID.fromString("123e4567-e89b-12d3-a456-426614174014");
        LeadSearchAsyncSnapshotResponse snapshot = new LeadSearchAsyncSnapshotResponse(
            requestId,
            LeadSearchStatus.PROCESSING,
            "Running",
            new LeadSearchAsyncProgress(0, 1, 0),
            List.of(),
            List.of(),
            Instant.parse("2026-03-21T12:00:00Z"),
            Instant.parse("2026-03-21T12:00:00Z"),
            null
        );
        when(leadSearchAsyncService.getSnapshot(requestId)).thenReturn(Optional.of(snapshot));
        when(streamService.subscribe(requestId)).thenReturn(new SseEmitter());

        mockMvc.perform(get("/api/leads/search/{requestId}/events", requestId))
            .andExpect(status().isOk());
    }
}
