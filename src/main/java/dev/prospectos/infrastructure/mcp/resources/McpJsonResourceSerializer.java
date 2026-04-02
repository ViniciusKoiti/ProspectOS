package dev.prospectos.infrastructure.mcp.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
class McpJsonResourceSerializer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize MCP resource payload", exception);
        }
    }
}
