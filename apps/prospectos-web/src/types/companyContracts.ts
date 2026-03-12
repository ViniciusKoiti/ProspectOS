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

export type Score = z.infer<typeof scoreSchema>;
export type Company = z.infer<typeof companySchema>;
