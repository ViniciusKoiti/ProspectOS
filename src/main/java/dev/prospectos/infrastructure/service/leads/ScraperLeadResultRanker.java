package dev.prospectos.infrastructure.service.leads;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dev.prospectos.api.dto.CompanyCandidateDTO;
import dev.prospectos.api.dto.LeadResultDTO;

final class ScraperLeadResultRanker {

    List<LeadResultDTO> deduplicateSortAndLimit(List<LeadResultDTO> leads, int limit) {
        Map<String, LeadResultDTO> uniqueByLeadKey = new LinkedHashMap<>();
        for (LeadResultDTO lead : leads) {
            if (lead == null || lead.leadKey() == null || lead.leadKey().isBlank()) {
                continue;
            }
            uniqueByLeadKey.putIfAbsent(lead.leadKey(), lead);
        }

        return uniqueByLeadKey.values().stream()
            .sorted(leadPriorityComparator())
            .limit(limit)
            .toList();
    }

    private Comparator<LeadResultDTO> leadPriorityComparator() {
        return Comparator
            .comparingInt(this::websitePriority)
            .thenComparingInt(this::contactPriority)
            .thenComparing(lead -> lead.score() == null ? 0 : lead.score().value(), Comparator.reverseOrder())
            .thenComparing(lead -> lead.leadKey() == null ? "" : lead.leadKey());
    }

    private int websitePriority(LeadResultDTO lead) {
        if (lead == null || lead.candidate() == null || lead.candidate().websitePresence() == null) {
            return 1;
        }
        return lead.candidate().websitePresence() == CompanyCandidateDTO.WebsitePresence.NO_WEBSITE ? 0 : 1;
    }

    private int contactPriority(LeadResultDTO lead) {
        if (lead == null || lead.candidate() == null || lead.candidate().contacts() == null || lead.candidate().contacts().isEmpty()) {
            return 1;
        }
        return 0;
    }
}
