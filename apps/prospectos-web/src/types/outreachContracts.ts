import { z } from 'zod';

const entityIdSchema = z.union([z.string().min(1), z.number().int()]).transform((value) => String(value));

export const outreachSegmentValues = ['ALL', 'HAS_WEBSITE', 'NO_WEBSITE'] as const;
export const outreachSegmentSchema = z.enum(outreachSegmentValues);

export const outreachLeadStatusSchema = z.enum(['SENT', 'FAILED', 'REPLIED']);

export const outreachCampaignRequestSchema = z.object({
    segment: outreachSegmentSchema,
    limit: z.number().int().min(1).max(500),
});

export const outreachCampaignLeadSchema = z.object({
    leadId: entityIdSchema,
    companyName: z.string(),
    website: z.string().nullable(),
    status: outreachLeadStatusSchema,
    detail: z.string().nullable(),
});

export const outreachCampaignSummarySchema = z.object({
    sent: z.number().int().min(0),
    failed: z.number().int().min(0),
    replied: z.number().int().min(0),
    total: z.number().int().min(0),
});

const rawLeadSchema = z.object({
    leadId: entityIdSchema.optional(),
    companyId: z.number().int().optional(),
    companyName: z.string(),
    website: z.string().nullable().optional(),
    status: outreachLeadStatusSchema,
    detail: z.string().nullable().optional(),
    message: z.string().nullable().optional(),
});

const rawOutreachCampaignResponseSchema = z.object({
    campaignId: entityIdSchema,
    segment: outreachSegmentSchema.optional(),
    websitePresence: outreachSegmentSchema.optional(),
    leads: z.array(rawLeadSchema),
    sent: z.number().int().min(0).optional(),
    failures: z.number().int().min(0).optional(),
    responses: z.number().int().min(0).optional(),
    summary: outreachCampaignSummarySchema.optional(),
});

export const outreachCampaignResponseSchema = rawOutreachCampaignResponseSchema.transform((payload) => {
    const leads: OutreachCampaignLead[] = payload.leads.map((lead, index) => ({
        leadId: lead.leadId ?? String(lead.companyId ?? index),
        companyName: lead.companyName,
        website: lead.website ?? null,
        status: lead.status,
        detail: lead.detail ?? lead.message ?? null,
    }));

    const summary = payload.summary ?? {
        sent: payload.sent ?? leads.filter((lead) => lead.status === 'SENT').length,
        failed: payload.failures ?? leads.filter((lead) => lead.status === 'FAILED').length,
        replied: payload.responses ?? leads.filter((lead) => lead.status === 'REPLIED').length,
        total: leads.length,
    };

    return {
        campaignId: payload.campaignId,
        segment: payload.segment ?? payload.websitePresence ?? 'ALL',
        leads,
        summary,
    };
});

export type OutreachSegment = z.infer<typeof outreachSegmentSchema>;
export type OutreachLeadStatus = z.infer<typeof outreachLeadStatusSchema>;
export type OutreachCampaignRequest = z.infer<typeof outreachCampaignRequestSchema>;
export type OutreachCampaignLead = z.infer<typeof outreachCampaignLeadSchema>;
export type OutreachCampaignSummary = z.infer<typeof outreachCampaignSummarySchema>;
export type OutreachCampaignResponse = z.infer<typeof outreachCampaignResponseSchema>;

