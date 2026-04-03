package dev.prospectos.infrastructure.service.discovery;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ApifyResponseMapperTest {

    private final ApifyResponseMapper mapper = new ApifyResponseMapper("apify");

    @Test
    void mapsDatasetItemToCandidate() {
        var items = new ApifyDatasetItem[] {
            new ApifyDatasetItem(
                "Smile Studio",
                null,
                "Orlando, FL, USA",
                null,
                "https://smilestudio.example.com",
                "+1 407-555-0101",
                null,
                "Dentist",
                null,
                List.of("Health")
            )
        };

        var results = mapper.toCandidates(items, 5);

        assertThat(results).hasSize(1);
        var candidate = results.getFirst();
        assertThat(candidate.name()).isEqualTo("Smile Studio");
        assertThat(candidate.website()).isEqualTo("https://smilestudio.example.com");
        assertThat(candidate.location()).isEqualTo("Orlando, FL, USA");
        assertThat(candidate.contacts()).containsExactly("+1 407-555-0101");
        assertThat(candidate.industry()).isEqualTo("Dentist");
        assertThat(candidate.sourceName()).isEqualTo("apify");
    }

    @Test
    void returnsEmptyWhenItemsAreMissing() {
        assertThat(mapper.toCandidates(null, 5)).isEmpty();
        assertThat(mapper.toCandidates(new ApifyDatasetItem[0], 5)).isEmpty();
    }
}
