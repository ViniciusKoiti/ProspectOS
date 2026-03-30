package dev.prospectos.infrastructure.service.discovery;

import dev.prospectos.api.mcp.QueryMetricsRecorder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscoverySourceRegistryTest {

    @Mock
    private QueryMetricsRecorder metricsRecorder;

    @Test
    void recordsSuccessfulSourceExecution() {
        LeadDiscoverySource source = new LeadDiscoverySource() {
            @Override
            public String sourceName() {
                return "amazon-location";
            }

            @Override
            public List<DiscoveredLeadCandidate> discover(DiscoveryContext context) {
                return List.of(
                    new DiscoveredLeadCandidate("A", "https://a", "health", "desc", "BR", List.of(), "amazon-location"),
                    new DiscoveredLeadCandidate("B", "https://b", "health", "desc", "BR", List.of(), "amazon-location")
                );
            }
        };

        var registry = new DiscoverySourceRegistry(List.of(source), metricsRecorder);
        var discovered = registry.discover(List.of("amazon-location"), new DiscoveryContext("dentistas", null, 5, null));

        assertThat(discovered).hasSize(2);
        verify(metricsRecorder).recordExecution(eq("amazon-location"), anyLong(), eq(true), eq(2));
    }

    @Test
    void recordsFailedSourceExecution() {
        LeadDiscoverySource source = new LeadDiscoverySource() {
            @Override
            public String sourceName() {
                return "open-cnpj";
            }

            @Override
            public List<DiscoveredLeadCandidate> discover(DiscoveryContext context) {
                throw new IllegalStateException("boom");
            }
        };

        var registry = new DiscoverySourceRegistry(List.of(source), metricsRecorder);

        assertThatThrownBy(() -> registry.discover(List.of("open-cnpj"), new DiscoveryContext("clinicas", null, 5, null)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("boom");

        verify(metricsRecorder).recordExecution(eq("open-cnpj"), anyLong(), eq(false), eq(0));
    }
}