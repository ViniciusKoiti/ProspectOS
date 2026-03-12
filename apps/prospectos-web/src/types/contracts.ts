import { z } from 'zod';

export const scoreSchema = z.object({
    value: z.number().int(),
    category: z.string(),
    reasoning: z.string(),
});

export const companySchema = z.object({
    id: z.number().int(),
    name: z.string(),
    industry: z.string().nullable(),
    website: z.string().nullable(),
    description: z.string().nullable(),
    employeeCount: z.number().int().nullable(),
    location: z.string().nullable(),
    score: scoreSchema.nullable(),
});

export const icpSchema = z.object({
    id: z.number().int(),
    name: z.string(),
    description: z.string().nullable(),
    targetIndustries: z.array(z.string()),
    regions: z.array(z.string()),
    targetTechnologies: z.array(z.string()),
    minEmployeeCount: z.number().int().nullable(),
    maxEmployeeCount: z.number().int().nullable(),
    targetRoles: z.array(z.string()),
    interestTheme: z.string().nullable(),
});

export const icpUpsertSchema = z.object({
    name: z.string().min(1),
    description: z.string().nullable(),
    industries: z.array(z.string()),
    regions: z.array(z.string()),
    targetRoles: z.array(z.string()),
    interestTheme: z.string().nullable(),
    targetTechnologies: z.array(z.string()),
    minEmployeeCount: z.number().int().nullable(),
    maxEmployeeCount: z.number().int().nullable(),
});

export const leadCandidateSchema = z.object({
    name: z.string(),
    website: z.string().nullable(),
    industry: z.string().nullable(),
    description: z.string().nullable(),
    size: z.string().nullable(),
    location: z.string().nullable(),
    contacts: z.array(z.string()),
});

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
    icpId: z.number().int().nullable(),
});

export type Company = z.infer<typeof companySchema>;
export type Icp = z.infer<typeof icpSchema>;
export type IcpUpsert = z.infer<typeof icpUpsertSchema>;
export type LeadResult = z.infer<typeof leadResultSchema>;
export type LeadSearchRequest = z.infer<typeof leadSearchRequestSchema>;
export type LeadSearchResponse = z.infer<typeof leadSearchResponseSchema>;
