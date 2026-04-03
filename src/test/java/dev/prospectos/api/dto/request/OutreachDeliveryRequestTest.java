package dev.prospectos.api.dto.request;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OutreachDeliveryRequestTest {

    @Test
    void acceptsHtmlOrTextPayload() {
        assertThat(new OutreachDeliveryRequest("Acme <sales@acme.com>", "lead@example.com", "Hello", "<p>Hi</p>", null, null).to())
            .isEqualTo("lead@example.com");
        assertThat(new OutreachDeliveryRequest("Acme <sales@acme.com>", "lead@example.com", "Hello", null, "Hi", null).text())
            .isEqualTo("Hi");
    }

    @Test
    void rejectsMissingRequiredFields() {
        assertThatThrownBy(() -> new OutreachDeliveryRequest(" ", "lead@example.com", "Hello", "<p>Hi</p>", null, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("from is required");
        assertThatThrownBy(() -> new OutreachDeliveryRequest("Acme <sales@acme.com>", " ", "Hello", "<p>Hi</p>", null, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("to is required");
        assertThatThrownBy(() -> new OutreachDeliveryRequest("Acme <sales@acme.com>", "lead@example.com", " ", "<p>Hi</p>", null, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("subject is required");
        assertThatThrownBy(() -> new OutreachDeliveryRequest("Acme <sales@acme.com>", "lead@example.com", "Hello", " ", " ", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("html or text is required");
    }
}
