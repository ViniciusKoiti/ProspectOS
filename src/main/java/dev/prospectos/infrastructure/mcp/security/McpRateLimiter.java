package dev.prospectos.infrastructure.mcp.security;

import dev.prospectos.infrastructure.mcp.config.McpSecurityProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
class McpRateLimiter {

    private final ConcurrentHashMap<String, ClientRateLimit> clientLimits = new ConcurrentHashMap<>();

    RateLimitResult check(String clientId, McpSecurityProperties.RateLimit rateLimit) {
        var now = Instant.now();
        var clientLimit = clientLimits.computeIfAbsent(clientId, ignored -> new ClientRateLimit(rateLimit.requestsPerMinute(), rateLimit.burstCapacity(), now));
        return clientLimit.tryConsume(now, rateLimit);
    }

    void applyHeaders(HttpServletResponse response, McpSecurityProperties.RateLimit rateLimit, RateLimitResult result) {
        response.addHeader("X-RateLimit-Limit", String.valueOf(rateLimit.requestsPerMinute()));
        response.addHeader("X-RateLimit-Remaining", String.valueOf(result.remaining()));
        response.addHeader("X-RateLimit-Reset", String.valueOf(result.resetTime().getEpochSecond()));
    }

    void writeExceededResponse(HttpServletResponse response, McpSecurityProperties.RateLimit rateLimit, RateLimitResult result) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        applyHeaders(response, rateLimit, result);
        response.addHeader("Retry-After", String.valueOf(result.retryAfterSeconds()));
        response.getWriter().write(String.format(
            "{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests. Try again in %d seconds.\",\"limit\":%d,\"remaining\":%d}",
            result.retryAfterSeconds(), rateLimit.requestsPerMinute(), result.remaining()
        ));
    }

    String clientId(HttpServletRequest request, String apiKeyHeader) {
        var apiKey = request.getHeader(apiKeyHeader);
        if (apiKey != null && !apiKey.isBlank()) {
            return "api-key:" + apiKey.substring(0, Math.min(8, apiKey.length())) + "...";
        }
        var forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return "ip:" + forwardedFor.split(",")[0].trim();
        }
        return "ip:" + request.getRemoteAddr();
    }

    record RateLimitResult(boolean allowed, int remaining, Instant resetTime, int retryAfterSeconds) {}

    private static final class ClientRateLimit {
        private final int maxTokens;
        private final int refillRate;
        private final AtomicInteger tokens;
        private volatile Instant lastRefill;

        private ClientRateLimit(int maxTokens, int burstCapacity, Instant now) {
            this.maxTokens = maxTokens;
            this.refillRate = maxTokens;
            this.tokens = new AtomicInteger(Math.min(burstCapacity, maxTokens));
            this.lastRefill = now;
        }

        private synchronized RateLimitResult tryConsume(Instant now, McpSecurityProperties.RateLimit rateLimit) {
            refillTokens(now);
            var resetTime = lastRefill.plus(rateLimit.windowSize());
            if (tokens.get() > 0) {
                return new RateLimitResult(true, tokens.decrementAndGet(), resetTime, 0);
            }
            var retryAfter = (int) java.time.Duration.between(now, resetTime).getSeconds();
            return new RateLimitResult(false, 0, resetTime, Math.max(retryAfter, 1));
        }

        private void refillTokens(Instant now) {
            var tokensToAdd = (int) (java.time.Duration.between(lastRefill, now).toSeconds() * refillRate / 60.0);
            if (tokensToAdd > 0) {
                tokens.set(Math.min(maxTokens, tokens.get() + tokensToAdd));
                lastRefill = now;
            }
        }
    }
}
