package dev.prospectos.core.enrichment;

import java.util.List;

import org.springframework.stereotype.Service;

import dev.prospectos.core.domain.Website;

@Service
public class CompanyEnrichmentService {

    private final DataNormalizer dataNormalizer;
    private final EmailFilter emailFilter;
    private final ContactProcessor contactProcessor;
    private final CompanyEnrichmentFieldNormalizer fieldNormalizer;
    private final CompanyEnrichmentQualityEvaluator qualityEvaluator;
    private final EnrichmentDegradedResultFactory degradedResultFactory;
    private final EnrichmentWebsiteResolver websiteResolver;

    public CompanyEnrichmentService(
        DataNormalizer dataNormalizer,
        EmailFilter emailFilter,
        ContactProcessor contactProcessor
    ) {
        this.dataNormalizer = dataNormalizer;
        this.emailFilter = emailFilter;
        this.contactProcessor = contactProcessor;
        this.fieldNormalizer = new CompanyEnrichmentFieldNormalizer(dataNormalizer);
        this.qualityEvaluator = new CompanyEnrichmentQualityEvaluator();
        this.websiteResolver = new EnrichmentWebsiteResolver();
        this.degradedResultFactory = new EnrichmentDegradedResultFactory(websiteResolver);
    }

    public EnrichmentResult enrichCompanyData(EnrichmentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("EnrichmentRequest cannot be null");
        }
        try {
            return enrich(request);
        } catch (Exception e) {
            return degradedResultFactory.create(request);
        }
    }

    public List<ValidatedContact> getPriorityContacts(EnrichmentResult result) {
        return emailFilter.getPriorityContacts(result.validatedContacts());
    }

    public List<ValidatedContact> getUsableContacts(EnrichmentResult result) {
        return emailFilter.getUsableContacts(result.validatedContacts());
    }

    public boolean isEnrichmentSufficient(EnrichmentResult result) {
        return result.normalizedCompanyName() != null
            && !result.normalizedCompanyName().trim().isEmpty()
            && result.website() != null
            && result.hasValidContacts()
            && result.quality().completenessScore() >= 0.5;
    }

    private EnrichmentResult enrich(EnrichmentRequest request) {
        NormalizedEnrichmentData normalized = fieldNormalizer.normalize(request);
        Website website = websiteResolver.parsePrimary(request.website());
        List<ValidatedContact> validatedContacts = emailFilter.filterAndValidateEmails(request.emails());
        EnrichmentQuality quality = qualityEvaluator.calculate(
            request,
            validatedContacts,
            normalized.normalizedName(),
            website
        );

        return new EnrichmentResult(
            normalized.normalizedName(),
            normalized.cleanDescription(),
            validatedContacts,
            normalized.normalizedPhone(),
            request.technologies(),
            normalized.standardizedIndustry(),
            normalized.size(),
            request.recentNews(),
            website,
            quality
        );
    }
}
