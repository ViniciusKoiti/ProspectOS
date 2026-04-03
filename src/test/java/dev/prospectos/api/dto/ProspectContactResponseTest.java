package dev.prospectos.api.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProspectContactResponseTest {

    @Test
    void exposesAllContactFields() {
        var response = new ProspectContactResponse("hello@acme.com", "Jane Doe", "Founder", 88, "hunter");

        assertThat(response.email()).isEqualTo("hello@acme.com");
        assertThat(response.name()).isEqualTo("Jane Doe");
        assertThat(response.position()).isEqualTo("Founder");
        assertThat(response.confidence()).isEqualTo(88);
        assertThat(response.source()).isEqualTo("hunter");
    }
}
