import { isAxiosError } from 'axios';

import type { LeadResult } from '../../types/leadContracts';

export const SEARCH_SOURCE_VALUES = ['in-memory', 'vector-company', 'cnpj-ws'] as const;

export type SearchSourceValue = typeof SEARCH_SOURCE_VALUES[number];

type ApiErrorPayload = {
    message?: unknown;
    error?: unknown;
};

const CSV_HEADERS = [
    'leadKey',
    'companyName',
    'website',
    'industry',
    'description',
    'size',
    'location',
    'contacts',
    'scoreValue',
    'scoreCategory',
    'scoreReasoning',
    'sourceName',
    'sourceUrl',
    'collectedAt',
] as const;

export function isSearchSourceValue(value: string): value is SearchSourceValue {
    return SEARCH_SOURCE_VALUES.some((candidate) => candidate === value);
}

export function getScoreBadgeVariant(category: string): 'success' | 'warning' | 'neutral' {
    const normalized = category.trim().toUpperCase();

    if (normalized === 'HOT') {
        return 'success';
    }

    if (normalized === 'WARM') {
        return 'warning';
    }

    return 'neutral';
}

function escapeCsvCell(value: string): string {
    const normalized = value.replaceAll('"', '""');

    if (normalized.includes(',') || normalized.includes('\n') || normalized.includes('"')) {
        return `"${normalized}"`;
    }

    return normalized;
}

function toCsvRow(values: string[]): string {
    return values.map(escapeCsvCell).join(',');
}

export function buildSearchResultsCsv(leads: LeadResult[]): string {
    const rows = leads.map((lead) =>
        toCsvRow([
            lead.leadKey,
            lead.candidate.name,
            lead.candidate.website ?? '',
            lead.candidate.industry ?? '',
            lead.candidate.description ?? '',
            lead.candidate.size ?? '',
            lead.candidate.location ?? '',
            lead.candidate.contacts.join(' | '),
            String(lead.score.value),
            lead.score.category,
            lead.score.reasoning,
            lead.source.sourceName,
            lead.source.sourceUrl ?? '',
            lead.source.collectedAt,
        ])
    );

    return [toCsvRow(Array.from(CSV_HEADERS)), ...rows].join('\n');
}

export function parseApiErrorMessage(error: unknown): string | null {
    if (isAxiosError(error)) {
        const responseData = error.response?.data;

        if (typeof responseData === 'string' && responseData.trim().length > 0) {
            return responseData.trim();
        }

        if (responseData && typeof responseData === 'object') {
            const payload = responseData as ApiErrorPayload;

            if (typeof payload.message === 'string' && payload.message.trim().length > 0) {
                return payload.message.trim();
            }

            if (typeof payload.error === 'string' && payload.error.trim().length > 0) {
                return payload.error.trim();
            }
        }

        if (!error.response) {
            return 'Unable to reach the backend API. Verify local backend is running.';
        }

        if (typeof error.message === 'string' && error.message.trim().length > 0) {
            return error.message.trim();
        }

        return `Request failed with status ${error.response.status}.`;
    }

    if (error instanceof Error && error.message.trim().length > 0) {
        return error.message.trim();
    }

    return null;
}

export function mergeWithFallbackError(fallbackMessage: string, detail: string | null): string {
    if (!detail) {
        return fallbackMessage;
    }

    if (detail.localeCompare(fallbackMessage, undefined, { sensitivity: 'accent' }) === 0) {
        return fallbackMessage;
    }

    return `${fallbackMessage} ${detail}`;
}
