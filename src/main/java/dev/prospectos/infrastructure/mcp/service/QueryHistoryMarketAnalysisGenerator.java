package dev.prospectos.infrastructure.mcp.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.mcp.CompetitorData;
import dev.prospectos.api.mcp.MarketAnalysisData;

final class QueryHistoryMarketAnalysisGenerator {

    private final CompanyDataService companyDataService;

    QueryHistoryMarketAnalysisGenerator(CompanyDataService companyDataService) {
        this.companyDataService = companyDataService;
    }

    MarketAnalysisData generate(String country, String industry) {
        List<dev.prospectos.api.dto.CompanyDTO> companies = companyDataService.findAllCompanies().stream()
            .filter(company -> contains(company.location(), country))
            .filter(company -> contains(company.industry(), industry))
            .toList();
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("marketSize", companies.size());
        metrics.put("growthRate", companies.isEmpty() ? 0.0d : 0.05d);
        metrics.put("competitionLevel", Math.min(1.0d, companies.size() / 10.0d));
        metrics.put("averageCompanySize", companies.stream().mapToInt(company -> company.employeeCount() == null ? 0 : company.employeeCount()).average().orElse(0.0d));
        metrics.put("digitalMaturity", companies.stream().mapToInt(company -> company.website() == null || company.website().isBlank() ? 0 : 1).average().orElse(0.0d));
        metrics.put("regulatoryComplexity", 0.3d);
        return new MarketAnalysisData(country, industry, metrics, insights(country, industry, companies), competitors(companies));
    }

    private Map<String, CompetitorData> competitors(List<dev.prospectos.api.dto.CompanyDTO> companies) {
        return companies.stream().limit(3).collect(LinkedHashMap::new, (map, company) -> map.put(company.name(), new CompetitorData(company.name(), 0.0d, List.of("Known local presence", "Persisted in ProspectOS dataset"), List.of("Coverage limited to internal dataset", "Requires additional enrichment"))), LinkedHashMap::putAll);
    }

    private List<String> insights(String country, String industry, List<dev.prospectos.api.dto.CompanyDTO> companies) {
        return List.of(
            "Persisted companies matching " + industry + " in " + country + ": " + companies.size(),
            companies.stream().anyMatch(company -> company.website() != null && !company.website().isBlank()) ? "Internal dataset already contains companies with active websites" : "Current internal dataset has limited website coverage",
            companies.isEmpty() ? "Use MCP search tools to expand this market dataset" : "Use MCP enrichment to deepen the existing market dataset"
        );
    }

    private boolean contains(String value, String fragment) {
        return value != null && value.toLowerCase().contains(fragment.toLowerCase());
    }
}
