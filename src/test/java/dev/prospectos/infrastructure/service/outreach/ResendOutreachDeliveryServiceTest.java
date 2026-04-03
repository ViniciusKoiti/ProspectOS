package dev.prospectos.infrastructure.service.outreach;

import java.net.URI;
import java.time.Duration;

import dev.prospectos.api.dto.request.OutreachDeliveryRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResendOutreachDeliveryServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Test
    void sendsEmailThroughResend() {
        var service = createService(true, "key");
        var request = new OutreachDeliveryRequest("Acme <sales@acme.com>", "lead@example.com", "Hello", "<p>Hi</p>", null, null);
        when(restTemplate.postForEntity(any(URI.class), any(HttpEntity.class), eq(ResendEmailResponse.class)))
            .thenReturn(ResponseEntity.ok(new ResendEmailResponse("email_123")));

        var response = service.send(request);

        assertThat(response.deliveryId()).isEqualTo("email_123");
        assertThat(response.status()).isEqualTo("SENT");

        var uriCaptor = ArgumentCaptor.forClass(URI.class);
        verify(restTemplate).postForEntity(uriCaptor.capture(), any(HttpEntity.class), eq(ResendEmailResponse.class));
        assertThat(uriCaptor.getValue().toString()).isEqualTo("https://api.resend.com/emails");
    }

    @Test
    void rejectsDisabledProvider() {
        var service = createService(false, "key");

        assertThatThrownBy(() -> service.send(new OutreachDeliveryRequest("Acme <sales@acme.com>", "lead@example.com", "Hello", "<p>Hi</p>", null, null)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Resend delivery is disabled");
        verifyNoInteractions(restTemplate);
    }

    @Test
    void rejectsMissingApiKey() {
        var service = createService(true, " ");

        assertThatThrownBy(() -> service.send(new OutreachDeliveryRequest("Acme <sales@acme.com>", "lead@example.com", "Hello", "<p>Hi</p>", null, null)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("API key is required");
        verifyNoInteractions(restTemplate);
    }

    @Test
    void wrapsClientFailures() {
        var service = createService(true, "key");
        when(restTemplate.postForEntity(any(URI.class), any(HttpEntity.class), eq(ResendEmailResponse.class)))
            .thenThrow(new RestClientException("boom"));

        assertThatThrownBy(() -> service.send(new OutreachDeliveryRequest("Acme <sales@acme.com>", "lead@example.com", "Hello", "<p>Hi</p>", null, null)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Resend delivery failed");
    }

    private ResendOutreachDeliveryService createService(boolean enabled, String apiKey) {
        return new ResendOutreachDeliveryService(restTemplate, new ResendProperties(enabled, apiKey, null, Duration.ofSeconds(15)));
    }
}
