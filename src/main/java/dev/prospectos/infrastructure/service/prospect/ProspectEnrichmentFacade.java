package dev.prospectos.infrastructure.service.prospect;

import dev.prospectos.api.ProspectEnrichService;
import dev.prospectos.api.dto.ProspectEnrichRequest;
import dev.prospectos.api.dto.ProspectEnrichResponse;
import dev.prospectos.ai.client.ScraperClientInterface;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.Website;
import dev.prospectos.core.enrichment.CompanyEnrichmentService;
import dev.prospectos.core.enrichment.ContactProcessor;
import dev.prospectos.core.enrichment.EnrichmentRequest;
import dev.prospectos.core.enrichment.EnrichmentResult;
import dev.prospectos.core.enrichment.ScraperDataMapper;
import org.springframework.stereotype.Service;

@Service
public class ProspectEnrichmentFacade {

    private final ProspectEnrichService prospectEnrichService;
    private final ScraperClientInterface scraperClient;
    private final CompanyEnrichmentService companyEnrichmentService;
    private final ContactProcessor contactProcessor;

    public ProspectEnrichmentFacade(
        ProspectEnrichService prospectEnrichService,
        ScraperClientInterface scraperClient,
        CompanyEnrichmentService companyEnrichmentService,
        ContactProcessor contactProcessor
    ) {
        this.prospectEnrichService = prospectEnrichService;
        this.scraperClient = scraperClient;
        this.companyEnrichmentService = companyEnrichmentService;
        this.contactProcessor = contactProcessor;
    }

    public ProspectEnrichResponse enrich(ProspectEnrichRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        String name = request.name() != null ? request.name().trim() : "";
        String website = request.website() != null ? request.website().trim() : "";

        if (name.isEmpty() || website.isEmpty()) {
            throw new IllegalArgumentException("Name and website are required");
        }

        String industry = request.industry() != null ? request.industry().trim() : null;

        EnrichmentResult enrichmentResult = null;
        ScraperClientInterface.ScrapingResponse response = scraperClient.scrapeWebsiteSync(website, false);
        if (response.success() && response.data() != null) {
            EnrichmentRequest enrichmentRequest = ScraperDataMapper.fromScraperData(response.data(), website);
            enrichmentRequest = mergeWithRequest(enrichmentRequest, name, industry);
            enrichmentResult = companyEnrichmentService.enrichCompanyData(enrichmentRequest);
        }

        Company company = buildCompany(name, website, industry, enrichmentResult);
        if (enrichmentResult != null) {
            applyEnrichment(company, enrichmentResult);
        }

        String analysis = prospectEnrichService.enrichCompany(company);

        return new ProspectEnrichResponse(
            company.getName(),
            company.getWebsite().getUrl(),
            company.getIndustry(),
            analysis
        );
    }

    private EnrichmentRequest mergeWithRequest(
        EnrichmentRequest enrichmentRequest,
        String fallbackName,
        String fallbackIndustry
    ) {
        return new EnrichmentRequest(
            firstNonBlank(enrichmentRequest.companyName(), fallbackName),
            enrichmentRequest.description(),
            enrichmentRequest.emails(),
            enrichmentRequest.phone(),
            enrichmentRequest.technologies(),
            firstNonBlank(enrichmentRequest.industry(), fallbackIndustry),
            enrichmentRequest.size(),
            enrichmentRequest.recentNews(),
            enrichmentRequest.website()
        );
    }

    private Company buildCompany(
        String fallbackName,
        String websiteUrl,
        String fallbackIndustry,
        EnrichmentResult enrichmentResult
    ) {
        String name = fallbackName;
        String industry = fallbackIndustry;
        Website website = Website.of(websiteUrl);

        if (enrichmentResult != null) {
            if (enrichmentResult.normalizedCompanyName() != null &&
                !enrichmentResult.normalizedCompanyName().isBlank()) {
                name = enrichmentResult.normalizedCompanyName().trim();
            }
            if (enrichmentResult.standardizedIndustry() != null &&
                !enrichmentResult.standardizedIndustry().isBlank()) {
                industry = enrichmentResult.standardizedIndustry().trim();
            }
            if (enrichmentResult.website() != null) {
                website = enrichmentResult.website();
            }
        }

        return Company.create(name, website, industry);
    }

    private void applyEnrichment(Company company, EnrichmentResult enrichmentResult) {
        if (enrichmentResult.cleanDescription() != null &&
            !enrichmentResult.cleanDescription().isBlank()) {
            company.setDescription(enrichmentResult.cleanDescription().trim());
        }
        if (enrichmentResult.size() != null) {
            company.setSize(enrichmentResult.size());
        }
        contactProcessor.processContacts(enrichmentResult.validatedContacts())
            .forEach(company::addContact);
    }

    private String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary.trim();
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback.trim();
        }
        return null;
    }
}
