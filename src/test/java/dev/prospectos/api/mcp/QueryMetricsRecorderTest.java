package dev.prospectos.api.mcp;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QueryMetricsRecorderTest {

    @Test
    void delegatesDefaultOperationAwareMethodToLegacySignature() {
        var recorder = new CapturingRecorder();

        recorder.recordExecution("nominatim", "dentists in brazil", 120L, true, 3);

        assertThat(recorder.provider).isEqualTo("nominatim");
        assertThat(recorder.durationMs).isEqualTo(120L);
        assertThat(recorder.success).isTrue();
        assertThat(recorder.resultCount).isEqualTo(3);
    }

    private static final class CapturingRecorder implements QueryMetricsRecorder {
        private String provider;
        private long durationMs;
        private boolean success;
        private int resultCount;

        @Override
        public void recordExecution(String provider, long durationMs, boolean success, int resultCount) {
            this.provider = provider;
            this.durationMs = durationMs;
            this.success = success;
            this.resultCount = resultCount;
        }
    }
}
