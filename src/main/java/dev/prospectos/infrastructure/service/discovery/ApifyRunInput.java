package dev.prospectos.infrastructure.service.discovery;

import java.util.List;

record ApifyRunInput(
    List<String> locations,
    List<String> keywords,
    List<String> urls,
    ApifyProxyConfiguration proxyConfiguration
) {
    static ApifyRunInput from(DiscoveryContext context, boolean useApifyProxy) {
        String query = context.query() == null ? "" : context.query().trim();
        int englishIndex = query.toLowerCase().lastIndexOf(" in ");
        int portugueseIndex = query.toLowerCase().lastIndexOf(" em ");
        int splitIndex = Math.max(englishIndex, portugueseIndex);
        if (splitIndex > 0) {
            int separatorSize = splitIndex == englishIndex ? 4 : 4;
            String keyword = query.substring(0, splitIndex).trim();
            String location = query.substring(splitIndex + separatorSize).trim();
            return new ApifyRunInput(List.of(location), List.of(keyword), List.of(), new ApifyProxyConfiguration(useApifyProxy));
        }
        return new ApifyRunInput(List.of(), List.of(query), List.of(), new ApifyProxyConfiguration(useApifyProxy));
    }
}

record ApifyProxyConfiguration(boolean useApifyProxy) {
}
