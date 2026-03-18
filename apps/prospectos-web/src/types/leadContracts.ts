import { z } from 'zod';

import { companySchema, scoreSchema } from './companyContracts';

export const websitePresenceValues = ['HAS_WEBSITE', 'NO_WEBSITE', 'UNKNOWN'] as const;
export const websitePresenceSchema = z.enum(websitePresenceValues);

const leadCandidateBaseSchema = z.object({
    name: z.string(),
    website: z.string().nullable(),
    industry: z.string().nullable(),
    description: z.string().nullable(),
    size: z.string().nullable(),
    location: z.string().nullable(),
    contacts: z.array(z.string()),
});

export const leadCandidateSchema = leadCandidateBaseSchema.extend({
    websitePresence: websitePresenceSchema.optional(),
}).transform((candidate) => ({
    ...candidate,
    websitePresence: candidate.websitePresence ?? (candidate.website && candidate.website.trim().length > 0 ? 'HAS_WEBSITE' : 'NO_WEBSITE'),
}));

export const acceptLeadCandidateSchema = leadCandidateBaseSchema;

export const sourceProvenanceSchema = z.object({
    sourceName: z.string(),
    sourceUrl: z.string().nullable(),
    collectedAt: z.string().datetime({ offset: true }),
});

export const leadResultSchema = z.object({
    candidate: leadCandidateSchema,
    score: scoreSchema,
    source: sourceProvenanceSchema,
    leadKey: z.string(),
});

export const acceptLeadRequestSchema = z.object({
    leadKey: z.string().min(1),
    candidate: acceptLeadCandidateSchema,
    score: scoreSchema.nullable(),
    source: sourceProvenanceSchema,
});

export const acceptLeadResponseSchema = z.object({
    company: companySchema,
    message: z.string(),
});

export const leadSearchStatusSchema = z.enum(['COMPLETED', 'PROCESSING', 'FAILED']);

export const leadSearchResponseSchema = z.object({
    status: leadSearchStatusSchema,
    leads: z.array(leadResultSchema),
    requestId: z.string().uuid(),
    message: z.string().nullable(),
});

export const leadSearchRequestSchema = z.object({
    query: z.string().min(1),
    limit: z.number().int().min(1).max(100),
    sources: z.array(z.string()).default([]),
    icpId: z.union([z.string().regex(/^-?\d+$/), z.number().finite()]).nullable(),
});

export type LeadCandidate = z.infer<typeof leadCandidateSchema>;
export type WebsitePresence = z.infer<typeof websitePresenceSchema>;
export type SourceProvenance = z.infer<typeof sourceProvenanceSchema>;
export type LeadResult = z.infer<typeof leadResultSchema>;
export type AcceptLeadRequest = z.infer<typeof acceptLeadRequestSchema>;
export type AcceptLeadResponse = z.infer<typeof acceptLeadResponseSchema>;
export type LeadSearchRequest = z.infer<typeof leadSearchRequestSchema>;
export type LeadSearchResponse = z.infer<typeof leadSearchResponseSchema>;
