import { z } from 'zod';

import type { Icp, IcpUpsert } from '../../types/contracts';

function blankToNull(value: string | null | undefined) {
    const trimmed = value?.trim() ?? '';
    return trimmed.length > 0 ? trimmed : null;
}

function parseOptionalInt(value: string | number | null | undefined, field: string, ctx: z.RefinementCtx) {
    if (value === '' || value === null || value === undefined) {
        return null;
    }

    const parsed = typeof value === 'number' ? value : Number(value);

    if (!Number.isInteger(parsed) || parsed < 0) {
        ctx.addIssue({
            code: z.ZodIssueCode.custom,
            message: `${field} must be a non-negative integer`,
        });
        return z.NEVER;
    }

    return parsed;
}

function uniqueLines(value: string) {
    return Array.from(
        new Set(
            value
                .split(/[\n,;]+/)
                .map((item) => item.trim())
                .filter(Boolean)
        )
    );
}

export const icpFormSchema = z
    .object({
        name: z.string().trim().min(1, 'Name is required'),
        description: z.string().default(''),
        industriesText: z.string().default(''),
        regionsText: z.string().default(''),
        targetRolesText: z.string().default(''),
        interestTheme: z.string().default(''),
        targetTechnologiesText: z.string().default(''),
        minEmployeeCount: z
            .union([z.string(), z.number(), z.null(), z.undefined()])
            .transform((value, ctx) => parseOptionalInt(value, 'Minimum employee count', ctx)),
        maxEmployeeCount: z
            .union([z.string(), z.number(), z.null(), z.undefined()])
            .transform((value, ctx) => parseOptionalInt(value, 'Maximum employee count', ctx)),
    })
    .superRefine((value, ctx) => {
        if (
            value.minEmployeeCount !== null &&
            value.maxEmployeeCount !== null &&
            value.maxEmployeeCount < value.minEmployeeCount
        ) {
            ctx.addIssue({
                code: z.ZodIssueCode.custom,
                path: ['maxEmployeeCount'],
                message: 'Maximum employee count must be greater than or equal to minimum employee count',
            });
        }
    });

export type IcpFormInput = z.input<typeof icpFormSchema>;
export type IcpFormValues = z.output<typeof icpFormSchema>;

function joinLines(items: string[]) {
    return items.join('\n');
}

export function createIcpFormDefaultValues(icp?: Icp): IcpFormInput {
    if (!icp) {
        return {
            name: '',
            description: '',
            industriesText: '',
            regionsText: '',
            targetRolesText: '',
            interestTheme: '',
            targetTechnologiesText: '',
            minEmployeeCount: '',
            maxEmployeeCount: '',
        };
    }

    return {
        name: icp.name,
        description: icp.description ?? '',
        industriesText: joinLines(icp.targetIndustries),
        regionsText: joinLines(icp.regions),
        targetRolesText: joinLines(icp.targetRoles),
        interestTheme: icp.interestTheme ?? '',
        targetTechnologiesText: joinLines(icp.targetTechnologies),
        minEmployeeCount: icp.minEmployeeCount ?? '',
        maxEmployeeCount: icp.maxEmployeeCount ?? '',
    };
}

export function toIcpUpsert(values: IcpFormValues): IcpUpsert {
    return {
        name: values.name.trim(),
        description: blankToNull(values.description),
        industries: uniqueLines(values.industriesText),
        regions: uniqueLines(values.regionsText),
        targetRoles: uniqueLines(values.targetRolesText),
        interestTheme: blankToNull(values.interestTheme),
        targetTechnologies: uniqueLines(values.targetTechnologiesText),
        minEmployeeCount: values.minEmployeeCount,
        maxEmployeeCount: values.maxEmployeeCount,
    };
}

