package dev.prospectos.infrastructure.service.prospect;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HunterResponseMapperTest {

    private final HunterResponseMapper mapper = new HunterResponseMapper();

    @Test
    void mapsAndOrdersContactsByConfidence() {
        var response = new HunterDomainSearchResponse(new HunterDomainSearchData(List.of(
            new HunterEmailEntry("low@acme.com", "Low", "Confidence", null, 20),
            new HunterEmailEntry("high@acme.com", "High", "Confidence", "Founder", 91)
        )));

        var contacts = mapper.toContacts(response, 5);

        assertThat(contacts).hasSize(2);
        assertThat(contacts.getFirst().email()).isEqualTo("high@acme.com");
        assertThat(contacts.getFirst().name()).isEqualTo("High Confidence");
        assertThat(contacts.getFirst().position()).isEqualTo("Founder");
        assertThat(contacts.getFirst().source()).isEqualTo("hunter");
    }
}
