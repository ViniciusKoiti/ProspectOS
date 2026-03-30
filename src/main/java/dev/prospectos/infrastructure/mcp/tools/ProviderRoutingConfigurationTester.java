package dev.prospectos.infrastructure.mcp.tools;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Component
class ProviderRoutingConfigurationTester {

    private final Random random = new Random();

    Map<String, Object> test(List<String> providers) {
        var results = new LinkedHashMap<String, Object>();
        results.put("testStatus", "COMPLETED");
        results.put("totalTests", 10);
        results.put("successfulTests", 8 + random.nextInt(2));
        results.put("averageResponseTime", 750 + random.nextInt(500));
        results.put("overallSuccessRate", 0.85 + (random.nextDouble() * 0.1));
        results.put("providerResults", providers.stream().collect(Collectors.toMap(
            provider -> provider,
            provider -> Map.of(
                "responseTime", 600 + random.nextInt(400),
                "successRate", 0.8 + (random.nextDouble() * 0.15),
                "errors", random.nextInt(3)
            )
        )));
        return results;
    }
}
