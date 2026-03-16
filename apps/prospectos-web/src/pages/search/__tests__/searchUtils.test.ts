import { describe, expect, it } from 'vitest';

import type { LeadResult } from '../../../types/leadContracts';
import {
    buildSearchResultsCsv,
    getScoreBadgeVariant,
    isSearchSourceValue,
    mergeWithFallbackError,
    parseApiErrorMessage,
} from '../searchUtils';

function createLead(): LeadResult {
    return {
        leadKey: 'lead-1',
        candidate: {
            name: 'Alpha, "One"',
            website: 'https://alpha.example',
            industry: 'Software',
            description: 'Platform\nVendor',
            size: 'MEDIUM',
            location: 'Sao Paulo',
            contacts: ['hello@alpha.example', 'ceo@alpha.example'],
        },
        score: {
            value: 91,
            category: 'HOT',
            reasoning: 'Strong fit',
        },
        source: {
            sourceName: 'in-memory',
            sourceUrl: 'https://source.example',
            collectedAt: '2026-03-16T10:00:00Z',
        },
    };
}

describe('searchUtils', () => {
    it('validates supported search sources', () => {
        expect(isSearchSourceValue('in-memory')).toBe(true);
        expect(isSearchSourceValue('vector-company')).toBe(true);
        expect(isSearchSourceValue('cnpj-ws')).toBe(true);
        expect(isSearchSourceValue('other-source')).toBe(false);
    });

    it('maps score categories to badge variants', () => {
        expect(getScoreBadgeVariant('HOT')).toBe('success');
        expect(getScoreBadgeVariant('warm')).toBe('warning');
        expect(getScoreBadgeVariant('cold')).toBe('neutral');
    });

    it('builds csv with escaped fields', () => {
        const csv = buildSearchResultsCsv([createLead()]);

        expect(csv).toContain('leadKey,companyName,website,industry');
        expect(csv).toContain('"Alpha, ""One"""');
        expect(csv).toContain('"Platform\nVendor"');
        expect(csv).toContain('hello@alpha.example | ceo@alpha.example');
    });

    it('parses axios-like api error messages', () => {
        const message = parseApiErrorMessage({
            isAxiosError: true,
            response: {
                status: 503,
                data: {
                    message: 'Backend indisponível',
                },
            },
            message: 'Request failed',
        });

        expect(message).toBe('Backend indisponível');
    });

    it('returns connection fallback when axios error has no response', () => {
        const message = parseApiErrorMessage({
            isAxiosError: true,
            response: undefined,
            message: 'Network Error',
        });

        expect(message).toBe('Unable to reach the backend API. Verify local backend is running.');
    });

    it('merges fallback and detail without duplication', () => {
        expect(mergeWithFallbackError('Falha', 'Detalhe')).toBe('Falha Detalhe');
        expect(mergeWithFallbackError('Falha', 'Falha')).toBe('Falha');
        expect(mergeWithFallbackError('Falha', null)).toBe('Falha');
    });
});
