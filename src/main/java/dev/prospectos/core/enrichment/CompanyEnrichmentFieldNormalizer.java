package dev.prospectos.core.enrichment;

final class CompanyEnrichmentFieldNormalizer {

    private final DataNormalizer dataNormalizer;

    CompanyEnrichmentFieldNormalizer(DataNormalizer dataNormalizer) {
        this.dataNormalizer = dataNormalizer;
    }

    NormalizedEnrichmentData normalize(EnrichmentRequest request) {
        return new NormalizedEnrichmentData(
            dataNormalizer.normalizeCompanyName(request.companyName()),
            dataNormalizer.normalizeDescription(request.description()),
            dataNormalizer.normalizePhone(request.phone()),
            dataNormalizer.standardizeIndustry(request.industry()),
            dataNormalizer.mapCompanySize(request.size())
        );
    }
}
