import { z } from 'zod';

const entityIdSchema = z.union([z.string().regex(/^-?\d+$/), z.number().finite()]).transform((value) => String(value));

export const scoreSchema = z.object({
    value: z.number().int(),
    category: z.string(),
    reasoning: z.string(),
});

export const companyContactSchema = z.object({
    name: z.string(),
    email: z.string().email(),
    position: z.string().nullable(),
    phoneNumber: z.string().nullable(),
});

export const companySchema = z.object({
    id: entityIdSchema,
    name: z.string(),
    industry: z.string().nullable(),
    website: z.string().nullable(),
    description: z.string().nullable(),
    employeeCount: z.number().int().nullable(),
    location: z.string().nullable(),
    score: scoreSchema.nullable(),
    primaryContactEmail: z.string().nullable().optional().default(null),
    contactCount: z.number().int().min(0).optional().default(0),
});

export const companyContactsSchema = z.array(companyContactSchema);

export type Score = z.infer<typeof scoreSchema>;
export type CompanyContact = z.infer<typeof companyContactSchema>;
export type Company = z.infer<typeof companySchema>;
