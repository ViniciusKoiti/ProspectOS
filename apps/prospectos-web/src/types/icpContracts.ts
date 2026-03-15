import { z } from 'zod';

const entityIdSchema = z.union([z.string().regex(/^-?\d+$/), z.number().finite()]).transform((value) => String(value));

export const icpSchema = z.object({
    id: entityIdSchema,
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

export type Icp = z.infer<typeof icpSchema>;
export type IcpUpsert = z.infer<typeof icpUpsertSchema>;
