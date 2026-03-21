package dev.prospectos.infrastructure.service.outreach;

import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.OutreachCampaignService;
import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.request.OutreachCampaignCreateRequest;
import dev.prospectos.api.dto.response.OutreachCampaignResponse;
import dev.prospectos.api.dto.response.OutreachLeadStatusResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * In-memory outreach campaign execution.
 */
@Service
public class InMemoryOutreachCampaignService implements OutreachCampaignService {

    private static final int DEFAULT_LIMIT = 50;
    private static final int REPLIED_SCORE_THRESHOLD = 90;

    private final CompanyDataService companyDataService;

    public InMemoryOutreachCampaignService(CompanyDataService companyDataService) {
        this.companyDataService = companyDataService;
    }

    @Override
    public OutreachCampaignResponse startCampaign(OutreachCampaignCreateRequest request) {
        int limit = request.limit() == null ? DEFAULT_LIMIT : request.limit();
        List<OutreachLeadStatusResponse> leads = companyDataService.findAllCompanies().stream()
            .filter(company -> matchesSegment(company, request.segment()))
            .limit(limit)
            .map(this::toLeadStatus)
            .toList();
        OutreachCampaignCounters counters = OutreachCampaignCounters.from(leads);

        return new OutreachCampaignResponse(
            UUID.randomUUID(),
            request.segment(),
            leads,
            counters.sent(),
            counters.failures(),
            counters.responses(),
            "Outreach campaign started"
        );
    }

    private boolean matchesSegment(CompanyDTO company, OutreachCampaignCreateRequest.WebsitePresenceSegment segment) {
        return switch (segment) {
            case ALL -> true;
            case HAS_WEBSITE -> hasWebsite(company);
            case NO_WEBSITE -> !hasWebsite(company);
        };
    }

    private OutreachLeadStatusResponse toLeadStatus(CompanyDTO company) {
        OutreachLeadStatusResponse.DeliveryStatus status = resolveStatus(company);
        return new OutreachLeadStatusResponse(company.id(), company.name(), company.website(), status, messageFor(status));
    }

    private OutreachLeadStatusResponse.DeliveryStatus resolveStatus(CompanyDTO company) {
        if (!hasContact(company)) {
            return OutreachLeadStatusResponse.DeliveryStatus.FAILED;
        }
        int score = company.score() == null ? 0 : company.score().value();
        if (score >= REPLIED_SCORE_THRESHOLD) {
            return OutreachLeadStatusResponse.DeliveryStatus.REPLIED;
        }
        return OutreachLeadStatusResponse.DeliveryStatus.SENT;
    }

    private String messageFor(OutreachLeadStatusResponse.DeliveryStatus status) {
        return switch (status) {
            case REPLIED -> "Lead replied";
            case FAILED -> "No contact available";
            case SENT -> "Message sent";
        };
    }

    private boolean hasWebsite(CompanyDTO company) {
        return company.website() != null && !company.website().isBlank();
    }

    private boolean hasContact(CompanyDTO company) {
        return (company.primaryContactEmail() != null && !company.primaryContactEmail().isBlank())
            || (company.contactCount() != null && company.contactCount() > 0);
    }
}
