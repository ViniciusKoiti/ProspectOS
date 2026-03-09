package dev.prospectos.infrastructure.service.leads;

import java.time.Instant;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import dev.prospectos.ai.client.ScraperClientInterface;
import dev.prospectos.ai.client.ScrapingResponse;
import dev.prospectos.api.ICPDataService;
import dev.prospectos.api.LeadSearchService;
import dev.prospectos.api.dto.CompanyCandidateDTO;
import dev.prospectos.api.dto.ICPDto;
import dev.prospectos.api.dto.LeadResultDTO;
import dev.prospectos.api.dto.LeadSearchRequest;
import dev.prospectos.api.dto.LeadSearchResponse;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.api.dto.SourceProvenanceDTO;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.ICP;
import dev.prospectos.core.enrichment.CompanyEnrichmentService;
import dev.prospectos.core.enrichment.EnrichmentRequest;
import dev.prospectos.core.enrichment.EnrichmentResult;
import dev.prospectos.core.enrichment.ScraperDataMapper;
import dev.prospectos.core.util.LeadKeyGenerator;
import dev.prospectos.infrastructure.config.LeadSearchProperties;
import dev.prospectos.infrastructure.service.compliance.AllowedSourcesComplianceService;
import dev.prospectos.infrastructure.service.scoring.CompanyScoringService;

@Service
@Profile("development")
public class ScraperLeadSearchService implements LeadSearchService {

    private final ScraperClientInterface scraperClient;
    private final CompanyEnrichmentService enrichmentService;
    private final ICPDataService icpDataService;
    private final CompanyScoringService scoringService;
    private final ScraperLeadRequestResolver requestResolver;
    private final ScraperLeadCandidateFactory candidateFactory = new ScraperLeadCandidateFactory();
    private final ScraperLeadResponseFactory responseFactory = new ScraperLeadResponseFactory();

    public ScraperLeadSearchService(
        ScraperClientInterface scraperClient,
        CompanyEnrichmentService enrichmentService,
        ICPDataService icpDataService,
        CompanyScoringService scoringService,
        AllowedSourcesComplianceService complianceService,
        LeadSearchProperties properties
    ) {
        this.scraperClient = scraperClient;
        this.enrichmentService = enrichmentService;
        this.icpDataService = icpDataService;
        this.scoringService = scoringService;
        this.requestResolver = new ScraperLeadRequestResolver(complianceService, properties);
    }

    @Override
    public LeadSearchResponse searchLeads(LeadSearchRequest request) {
        ScraperLeadRequestContext context = requestResolver.resolve(request);
        ScrapingResponse scrapingResponse = scraperClient.scrapeWebsiteSync(context.query(), false);
        if (!scrapingResponse.success() || scrapingResponse.data() == null) {
            return responseFactory.failed(scrapingResponse.error());
        }

        EnrichmentRequest enrichmentRequest = ScraperDataMapper.fromScraperData(scrapingResponse.data(), context.query());
        EnrichmentResult enrichmentResult = enrichmentService.enrichCompanyData(enrichmentRequest);
        Company candidateCompany = candidateFactory.buildCandidateCompany(enrichmentResult, context.query());
        if (candidateCompany == null || context.limit() <= 0) {
            return responseFactory.noLeads();
        }

        ICP icp = loadIcp(requestResolver.resolveIcpId(request.icpId()));
        ScoreDTO score = scoringService.scoreCandidate(candidateCompany, icp);
        CompanyCandidateDTO candidate = candidateFactory.toCompanyCandidateDTO(candidateCompany, enrichmentResult);
        String sourceUrl = candidateFactory.resolveSourceUrl(enrichmentResult, context.query());
        String leadKey = LeadKeyGenerator.generate(sourceUrl, context.sourceName());
        SourceProvenanceDTO provenance = new SourceProvenanceDTO(context.sourceName(), sourceUrl, Instant.now());

        return responseFactory.completed(new LeadResultDTO(candidate, score, provenance, leadKey));
    }

    private ICP loadIcp(Long icpId) {
        ICPDto icpDto = icpDataService.findICP(icpId);
        if (icpDto == null) {
            throw new IllegalArgumentException("ICP not found with id: " + icpId);
        }
        return ScraperLeadIcpMapper.toDomainICP(icpDto);
    }
}
