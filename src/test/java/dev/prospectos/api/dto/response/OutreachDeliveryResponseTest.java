package dev.prospectos.api.dto.response;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OutreachDeliveryResponseTest {

    @Test
    void exposesDeliveryFields() {
        var response = new OutreachDeliveryResponse("email_123", "SENT", "Email delivered to Resend");

        assertThat(response.deliveryId()).isEqualTo("email_123");
        assertThat(response.status()).isEqualTo("SENT");
        assertThat(response.message()).isEqualTo("Email delivered to Resend");
    }
}
