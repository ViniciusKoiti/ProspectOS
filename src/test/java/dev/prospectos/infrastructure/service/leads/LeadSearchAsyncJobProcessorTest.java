package dev.prospectos.infrastructure.service.leads;

import dev.prospectos.api.LeadSearchService;
import dev.prospectos.api.dto.CompanyCandidateDTO;
import dev.prospectos.api.dto.LeadResultDTO;
import dev.prospectos.api.dto.LeadSearchRequest;
import dev.prospectos.api.dto.LeadSearchResponse;
import dev.prospectos.api.dto.LeadSearchStatus;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.api.dto.SourceProvenanceDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeadSearchAsyncJobProcessorTest {

    @Mock
    private LeadSearchService leadSearchService;

    @Test
    void processCompletesWithPartialFailuresWhenAtLeastOneSourceSucceeds() {
        LeadSearchAsyncProperties properties = new LeadSearchAsyncProperties(2, 5, 60);
        LeadSearchAsyncJobProcessor processor = new LeadSearchAsyncJobProcessor(
            leadSearchService,
            properties,
            new LeadSearchAsyncSseHub()
        );
        when(leadSearchService.searchLeads(any(LeadSearchRequest.class))).thenAnswer(invocation -> {
            LeadSearchRequest request = invocation.getArgument(0);
            if (request.sources().contains("in-memory")) {
                return new LeadSearchResponse(
                    LeadSearchStatus.COMPLETED,
                    List.of(sampleLead("lead-1", "in-memory")),
                    UUID.randomUUID(),
                    "ok"
                );
            }
            return new LeadSearchResponse(LeadSearchStatus.FAILED, List.of(), UUID.randomUUID(), "source failed");
        });

        LeadSearchRequest request = new LeadSearchRequest("agencias", 10, List.of("in-memory", "cnpj-ws"), 1L);
        LeadSearchAsyncJob job = new LeadSearchAsyncJob(UUID.randomUUID(), 10, List.of("in-memory", "cnpj-ws"), Instant.now());

        processor.process(job, request, List.of("in-memory", "cnpj-ws"));

        var snapshot = job.snapshot();
        assertThat(snapshot.status()).isEqualTo(LeadSearchStatus.COMPLETED);
        assertThat(snapshot.progress().failedSources()).isEqualTo(1);
        assertThat(snapshot.leads()).hasSize(1);
        assertThat(snapshot.message()).contains("partial");
    }

    @Test
    void processFailsWhenAllSourcesFail() {
        LeadSearchAsyncProperties properties = new LeadSearchAsyncProperties(2, 5, 60);
        LeadSearchAsyncJobProcessor processor = new LeadSearchAsyncJobProcessor(
            leadSearchService,
            properties,
            new LeadSearchAsyncSseHub()
        );
        when(leadSearchService.searchLeads(any(LeadSearchRequest.class)))
            .thenReturn(new LeadSearchResponse(LeadSearchStatus.FAILED, List.of(), UUID.randomUUID(), "source failed"));

        LeadSearchRequest request = new LeadSearchRequest("agencias", 10, List.of("in-memory", "cnpj-ws"), 1L);
        LeadSearchAsyncJob job = new LeadSearchAsyncJob(UUID.randomUUID(), 10, List.of("in-memory", "cnpj-ws"), Instant.now());

        processor.process(job, request, List.of("in-memory", "cnpj-ws"));

        var snapshot = job.snapshot();
        assertThat(snapshot.status()).isEqualTo(LeadSearchStatus.FAILED);
        assertThat(snapshot.progress().failedSources()).isEqualTo(2);
        assertThat(snapshot.leads()).isEmpty();
    }

    private LeadResultDTO sampleLead(String leadKey, String sourceName) {
        return new LeadResultDTO(
            new CompanyCandidateDTO(
                "Acme",
                null,
                "Software",
                "Desc",
                "SMALL",
                "Sao Paulo",
                List.of("contato@acme.com"),
                CompanyCandidateDTO.WebsitePresence.NO_WEBSITE
            ),
            new ScoreDTO(90, "HOT", "fit"),
            new SourceProvenanceDTO(sourceName, null, Instant.now()),
            leadKey
        );
    }
}
