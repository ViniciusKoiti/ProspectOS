package dev.prospectos.infrastructure.api.outreach;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.prospectos.api.OutreachDeliveryService;
import dev.prospectos.api.dto.request.OutreachDeliveryRequest;
import dev.prospectos.api.dto.response.OutreachDeliveryResponse;
import dev.prospectos.infrastructure.handler.ApiExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OutreachDeliveryControllerTest {

    private final OutreachDeliveryService outreachDeliveryService = mock(OutreachDeliveryService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new OutreachDeliveryController(outreachDeliveryService))
            .setControllerAdvice(new ApiExceptionHandler())
            .build();
    }

    @Test
    void sendsDelivery() throws Exception {
        when(outreachDeliveryService.send(any())).thenReturn(new OutreachDeliveryResponse("email_123", "SENT", "Email delivered to Resend"));

        mockMvc.perform(post("/api/outreach/deliveries")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new OutreachDeliveryRequest(
                    "Acme <sales@acme.com>",
                    "lead@example.com",
                    "Hello",
                    "<p>Hi</p>",
                    null,
                    null
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.deliveryId").value("email_123"))
            .andExpect(jsonPath("$.status").value("SENT"));
    }
}
