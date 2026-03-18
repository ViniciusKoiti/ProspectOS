package dev.prospectos.infrastructure.service.leads;

import java.time.Instant;
import java.util.List;

import dev.prospectos.ai.client.ScraperClientInterface;
import dev.prospectos.ai.client.ScrapingResponse;
import dev.prospectos.api.dto.CompanyCandidateDTO;
import dev.prospectos.api.dto.LeadResultDTO;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.api.dto.SourceProvenanceDTO;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.ICP;
import dev.prospectos.core.enrichment.CompanyEnrichmentService;
import dev.prospectos.core.enrichment.EnrichmentRequest;
import dev.prospectos.core.enrichment.EnrichmentResult;
import dev.prospectos.core.enrichment.ScraperDataMapper;
import dev.prospectos.core.util.LeadKeyGenerator;
import dev.prospectos.infrastructure.service.scoring.CompanyScoringService;

final class ScraperWebsiteLeadSearch {

    private final ScraperClientInterface scraperClient;
    private final CompanyEnrichmentService enrichmentService;
    private final CompanyScoringService scoringService;
    private final ScraperLeadCandidateFactory candidateFactory;

    ScraperWebsiteLeadSearch(
        ScraperClientInterface scraperClient,
        CompanyEnrichmentService enrichmentService,
        CompanyScoringService scoringService,
        ScraperLeadCandidateFactory candidateFactory
    ) {
        this.scraperClient = scraperClient;
        this.enrichmentService = enrichmentService;
        this.scoringService = scoringService;
        this.candidateFactory = candidateFactory;
    }

    List<LeadResultDTO> search(String source, ScraperLeadRequestContext context, ICP icp) {
        ScrapingResponse response = scraperClient.scrapeWebsiteSync(context.scraperQuery(), false);
        if (!response.success() || response.data() == null) {
            throw new IllegalStateException(response.error() == null ? "Scraper failed" : response.error());
        }

        EnrichmentRequest enrichmentRequest = ScraperDataMapper.fromScraperData(response.data(), context.scraperQuery());
        EnrichmentResult enrichmentResult = enrichmentService.enrichCompanyData(enrichmentRequest);
        Company candidateCompany = candidateFactory.buildCandidateCompany(enrichmentResult, context.scraperQuery());
        if (candidateCompany == null || context.limit() <= 0) {
            return List.of();
        }

        ScoreDTO score = scoringService.scoreCandidate(candidateCompany, icp);
        CompanyCandidateDTO candidate = candidateFactory.toCompanyCandidateDTO(candidateCompany, enrichmentResult);
        String sourceUrl = candidateFactory.resolveSourceUrl(enrichmentResult, context.scraperQuery());
        String leadKey = LeadKeyGenerator.generate(sourceUrl, source);
        SourceProvenanceDTO provenance = new SourceProvenanceDTO(source, sourceUrl, Instant.now());
        return List.of(new LeadResultDTO(candidate, score, provenance, leadKey));
    }
}
