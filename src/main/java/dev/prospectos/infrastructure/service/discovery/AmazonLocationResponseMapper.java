package dev.prospectos.infrastructure.service.discovery;

import java.util.List;
import java.util.Objects;

final class AmazonLocationResponseMapper {

    private final DiscoveryCandidateMapper candidateMapper;
    private final AmazonLocationFieldExtractor fieldExtractor;
    private final String sourceName;

    AmazonLocationResponseMapper(String sourceName) {
        this(new DiscoveryCandidateMapper(), new AmazonLocationFieldExtractor(), sourceName);
    }

    AmazonLocationResponseMapper(
        DiscoveryCandidateMapper candidateMapper,
        AmazonLocationFieldExtractor fieldExtractor,
        String sourceName
    ) {
        this.candidateMapper = candidateMapper;
        this.fieldExtractor = fieldExtractor;
        this.sourceName = sourceName;
    }

    List<DiscoveredLeadCandidate> toCandidates(List<AmazonLocationResultItem> resultItems, int limit) {
        if (resultItems == null || resultItems.isEmpty()) {
            return List.of();
        }
        int maxResults = Math.max(1, limit);
        return resultItems.stream()
            .map(this::toCandidate)
            .filter(Objects::nonNull)
            .limit(maxResults)
            .toList();
    }

    private DiscoveredLeadCandidate toCandidate(AmazonLocationResultItem item) {
        if (item == null) {
            return null;
        }
        return candidateMapper.toCandidate(
            sourceName,
            fieldExtractor.resolveName(item),
            fieldExtractor.resolveWebsite(item.contacts()),
            fieldExtractor.resolveIndustry(item.categories()),
            fieldExtractor.resolveDescription(item.placeType(), item.address()),
            fieldExtractor.resolveLocation(item.address()),
            fieldExtractor.resolveEmails(item.contacts())
        );
    }
}
