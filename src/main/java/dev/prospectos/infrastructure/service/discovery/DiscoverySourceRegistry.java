package dev.prospectos.infrastructure.service.discovery;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class DiscoverySourceRegistry {

    private final Map<String, LeadDiscoverySource> sourcesByName;

    DiscoverySourceRegistry(List<LeadDiscoverySource> sources) {
        this.sourcesByName = indexSources(sources);
    }

    List<DiscoveredLeadCandidate> discover(List<String> sourceNames, DiscoveryContext context) {
        List<DiscoveredLeadCandidate> discovered = new ArrayList<>();
        for (String sourceName : sourceNames) {
            LeadDiscoverySource source = sourcesByName.get(sourceName);
            if (source == null) {
                throw new IllegalArgumentException("Configured source without implementation: " + sourceName);
            }
            discovered.addAll(source.discover(context));
        }
        return discovered;
    }

    private Map<String, LeadDiscoverySource> indexSources(List<LeadDiscoverySource> sources) {
        Map<String, LeadDiscoverySource> indexed = new LinkedHashMap<>();
        for (LeadDiscoverySource source : sources) {
            indexed.put(source.sourceName(), source);
        }
        return indexed;
    }
}
