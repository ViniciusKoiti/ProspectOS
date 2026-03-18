import { describe, expect, it } from 'vitest';

import type { LeadResult } from '../../../types/leadContracts';
import {
    buildSearchResultsCsv,
    filterLeadsByWebsitePresence,
    getScoreBadgeVariant,
    getWebsitePresenceBadgeVariant,
    getWebsitePresenceLabel,
    isSearchSourceValue,
    isWebsitePresenceFilterValue,
    mergeWithFallbackError,
    parseApiErrorMessage,
} from '../searchUtils';

function createLead(websitePresence: LeadResult['candidate']['websitePresence']): LeadResult {
    return {
        leadKey: `lead-${websitePresence.toLowerCase()}`,
        candidate: {
            name: `Company ${websitePresence}`,
            website: websitePresence === 'HAS_WEBSITE' ? 'https://alpha.example' : null,
            websitePresence,
            industry: 'Software',
            description: null,
            size: 'MEDIUM',
            location: 'Sao Paulo',
            contacts: ['hello@alpha.example'],
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

    it('validates supported website presence filters', () => {
        expect(isWebsitePresenceFilterValue('all')).toBe(true);
        expect(isWebsitePresenceFilterValue('HAS_WEBSITE')).toBe(true);
        expect(isWebsitePresenceFilterValue('NO_WEBSITE')).toBe(true);
        expect(isWebsitePresenceFilterValue('UNKNOWN')).toBe(false);
    });

    it('filters leads by website presence and keeps all when filter is all', () => {
        const leads = [createLead('HAS_WEBSITE'), createLead('NO_WEBSITE'), createLead('UNKNOWN')];

        expect(filterLeadsByWebsitePresence(leads, 'all')).toHaveLength(3);
        expect(filterLeadsByWebsitePresence(leads, 'HAS_WEBSITE')).toHaveLength(1);
        expect(filterLeadsByWebsitePresence(leads, 'NO_WEBSITE')).toHaveLength(1);
    });

    it('maps website presence to badge variants and labels', () => {
        expect(getWebsitePresenceBadgeVariant('HAS_WEBSITE')).toBe('success');
        expect(getWebsitePresenceBadgeVariant('NO_WEBSITE')).toBe('warning');
        expect(getWebsitePresenceBadgeVariant('UNKNOWN')).toBe('neutral');
        expect(getWebsitePresenceLabel('HAS_WEBSITE', { hasWebsite: 'Com site', noWebsite: 'Sem site', unknown: 'Desconhecido' })).toBe('Com site');
        expect(getWebsitePresenceLabel('NO_WEBSITE', { hasWebsite: 'Com site', noWebsite: 'Sem site', unknown: 'Desconhecido' })).toBe('Sem site');
        expect(getWebsitePresenceLabel('UNKNOWN', { hasWebsite: 'Com site', noWebsite: 'Sem site', unknown: 'Desconhecido' })).toBe('Desconhecido');
    });

    it('maps score categories to badge variants', () => {
        expect(getScoreBadgeVariant('HOT')).toBe('success');
        expect(getScoreBadgeVariant('warm')).toBe('warning');
        expect(getScoreBadgeVariant('cold')).toBe('neutral');
    });

    it('builds csv with escaped fields', () => {
        const csv = buildSearchResultsCsv([createLead('HAS_WEBSITE')]);

        expect(csv).toContain('leadKey,companyName,website,industry');
        expect(csv).toContain('hello@alpha.example');
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
