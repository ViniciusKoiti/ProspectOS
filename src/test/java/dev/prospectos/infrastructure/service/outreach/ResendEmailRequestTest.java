package dev.prospectos.infrastructure.service.outreach;

import dev.prospectos.api.dto.request.OutreachDeliveryRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResendEmailRequestTest {

    @Test
    void mapsOutgoingPayload() {
        var request = ResendEmailRequest.from(new OutreachDeliveryRequest(
            "Acme <sales@acme.com>",
            "lead@example.com",
            "Hello",
            "<p>Hi</p>",
            null,
            "reply@acme.com"
        ));

        assertThat(request.from()).isEqualTo("Acme <sales@acme.com>");
        assertThat(request.to()).isEqualTo("lead@example.com");
        assertThat(request.subject()).isEqualTo("Hello");
        assertThat(request.html()).isEqualTo("<p>Hi</p>");
        assertThat(request.text()).isNull();
        assertThat(request.replyTo()).isEqualTo("reply@acme.com");
    }
}
