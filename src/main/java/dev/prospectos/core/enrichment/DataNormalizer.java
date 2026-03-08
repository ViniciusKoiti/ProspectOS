package dev.prospectos.core.enrichment;

import org.springframework.stereotype.Component;

import dev.prospectos.core.domain.CompanySize;

@Component
public class DataNormalizer {

    private final EnrichmentTextNormalizer textNormalizer = new EnrichmentTextNormalizer();
    private final IndustryStandardizer industryStandardizer = new IndustryStandardizer(textNormalizer);
    private final CompanySizeMapper sizeMapper = new CompanySizeMapper();

    public String normalizeCompanyName(String name) {
        return textNormalizer.normalizeCompanyName(name);
    }

    public String normalizeDescription(String description) {
        return textNormalizer.normalizeDescription(description);
    }

    public String standardizeIndustry(String industry) {
        return industryStandardizer.standardizeIndustry(industry);
    }

    public String normalizePhone(String phone) {
        return textNormalizer.normalizePhone(phone);
    }

    public CompanySize mapCompanySize(String sizeString) {
        return sizeMapper.mapCompanySize(sizeString);
    }
}

