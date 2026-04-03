package dev.prospectos.infrastructure.service.outreach;

import java.net.URI;

import dev.prospectos.api.OutreachDeliveryService;
import dev.prospectos.api.dto.request.OutreachDeliveryRequest;
import dev.prospectos.api.dto.response.OutreachDeliveryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class ResendOutreachDeliveryService implements OutreachDeliveryService {
    private static final Logger log = LoggerFactory.getLogger(ResendOutreachDeliveryService.class);

    private final RestTemplate restTemplate;
    private final ResendProperties properties;

    public ResendOutreachDeliveryService(RestTemplateBuilder restTemplateBuilder, ResendProperties properties) {
        this(restTemplateBuilder.setConnectTimeout(properties.normalizedTimeout()).setReadTimeout(properties.normalizedTimeout()).build(),
            properties);
    }

    ResendOutreachDeliveryService(RestTemplate restTemplate, ResendProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    @Override
    public OutreachDeliveryResponse send(OutreachDeliveryRequest request) {
        if (!properties.enabled()) {
            throw new IllegalStateException("Resend delivery is disabled");
        }
        validateApiKey();
        try {
            ResponseEntity<ResendEmailResponse> response = restTemplate.postForEntity(
                URI.create(properties.normalizedBaseUrl()),
                requestEntity(request),
                ResendEmailResponse.class
            );
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null || response.getBody().id() == null) {
                throw new IllegalStateException("Resend did not return a delivery id");
            }
            return new OutreachDeliveryResponse(response.getBody().id(), "SENT", "Email delivered to Resend");
        } catch (RestClientException exception) {
            log.warn("Resend delivery failed for recipient '{}': {}", request.to(), exception.getMessage());
            throw new IllegalStateException("Resend delivery failed", exception);
        }
    }

    private HttpEntity<ResendEmailRequest> requestEntity(OutreachDeliveryRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(properties.apiKey().trim());
        return new HttpEntity<>(ResendEmailRequest.from(request), headers);
    }

    private void validateApiKey() {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            throw new IllegalStateException("Resend API key is required when resend delivery is enabled");
        }
    }
}
