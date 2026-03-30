package dev.prospectos.api.mcp;

import java.util.List;
import java.util.Map;

/**
 * Manages provider routing decisions exposed through MCP.
 */
public interface ProviderRoutingService {

    RoutingUpdate updateRouting(RoutingStrategy strategy, List<String> providerPriority, Map<String, String> conditions);

    List<ProviderHealth> getProviderHealth();
}
