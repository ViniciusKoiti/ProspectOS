package dev.prospectos.infrastructure.mcp.resources;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class McpJsonResourceSerializerTest {

    private final McpJsonResourceSerializer serializer = new McpJsonResourceSerializer();

    @Test
    void serializesSimplePayloads() {
        assertThat(serializer.serialize(java.util.Map.of("status", "ok"))).contains("\"status\":\"ok\"");
    }

    @Test
    void rejectsUnsupportedObjectGraphs() {
        Object invalid = new Object() {
            @SuppressWarnings("unused")
            public final Object self = this;
        };

        assertThatThrownBy(() -> serializer.serialize(invalid))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Failed to serialize");
    }
}
