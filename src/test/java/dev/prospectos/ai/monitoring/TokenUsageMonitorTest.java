package dev.prospectos.ai.monitoring;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenUsageMonitorTest {

    @Test
    void startsWithZeroCounters() {
        TokenUsageMonitor monitor = new TokenUsageMonitor();

        assertThat(monitor.getTotalRequests()).isZero();
        assertThat(monitor.getTotalTokens()).isZero();
    }

    @Test
    void logRequestAccumulatesRequestsAndEstimatedTokens() {
        TokenUsageMonitor monitor = new TokenUsageMonitor();

        monitor.logRequest("12345678", "abcd");
        monitor.logRequest("1234", "1234");

        assertThat(monitor.getTotalRequests()).isEqualTo(2);
        assertThat(monitor.getTotalTokens()).isEqualTo(5);
    }

    @Test
    void printUsageSummaryDoesNotChangeCounters() {
        TokenUsageMonitor monitor = new TokenUsageMonitor();
        monitor.logRequest("1234", "1234");

        monitor.printUsageSummary();

        assertThat(monitor.getTotalRequests()).isEqualTo(1);
        assertThat(monitor.getTotalTokens()).isEqualTo(2);
    }

    @Test
    void estimateUsesCharacterDivisionByFourWithFloorBehavior() {
        TokenUsageMonitor monitor = new TokenUsageMonitor();

        monitor.logRequest("12345", "12");

        assertThat(monitor.getTotalRequests()).isEqualTo(1);
        assertThat(monitor.getTotalTokens()).isEqualTo(1);
    }

    @Test
    void printUsageSummaryWithNoRequestsKeepsCountersAtZero() {
        TokenUsageMonitor monitor = new TokenUsageMonitor();

        monitor.printUsageSummary();

        assertThat(monitor.getTotalRequests()).isZero();
        assertThat(monitor.getTotalTokens()).isZero();
    }
}
