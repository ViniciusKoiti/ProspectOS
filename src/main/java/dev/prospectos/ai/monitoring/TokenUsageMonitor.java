package dev.prospectos.ai.monitoring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple token usage monitoring for development
 */
@Slf4j
@Component
public class TokenUsageMonitor {
    
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong estimatedTokens = new AtomicLong(0);
    
    public void logRequest(String prompt, String response) {
        long requests = totalRequests.incrementAndGet();
        long tokens = estimatedTokens.addAndGet(estimateTokens(prompt, response));
        
        log.info("AI request #{} | Estimated tokens: ~{} | Total: ~{}",
                requests, estimateTokens(prompt, response), tokens);
    }
    
    public void printUsageSummary() {
        double estimatedCost = estimatedTokens.get() * 0.00002;
        log.info("Token usage summary | Total requests: {} | Estimated tokens: ~{} | Estimated cost: ${}",
            totalRequests.get(),
            estimatedTokens.get(),
            String.format("%.4f", estimatedCost));
    }
    
    private long estimateTokens(String prompt, String response) {
        return (prompt.length() + response.length()) / 4;
    }
    
    public long getTotalTokens() {
        return estimatedTokens.get();
    }
    
    public long getTotalRequests() {
        return totalRequests.get();
    }
}
