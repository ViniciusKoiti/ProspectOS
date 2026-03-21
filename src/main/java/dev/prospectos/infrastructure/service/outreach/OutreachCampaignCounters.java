package dev.prospectos.infrastructure.service.outreach;

import dev.prospectos.api.dto.response.OutreachLeadStatusResponse;

import java.util.List;

record OutreachCampaignCounters(int sent, int failures, int responses) {

    static OutreachCampaignCounters from(List<OutreachLeadStatusResponse> leads) {
        int sent = 0;
        int failures = 0;
        int responses = 0;

        for (OutreachLeadStatusResponse lead : leads) {
            if (lead.status() == OutreachLeadStatusResponse.DeliveryStatus.FAILED) {
                failures++;
                continue;
            }
            sent++;
            if (lead.status() == OutreachLeadStatusResponse.DeliveryStatus.REPLIED) {
                responses++;
            }
        }

        return new OutreachCampaignCounters(sent, failures, responses);
    }
}
