import { createElement } from 'react';
import { renderToStaticMarkup } from 'react-dom/server';
import { describe, expect, it, vi } from 'vitest';

import type { LeadResult } from '../../../types/leadContracts';
import SearchMatchInsights from '../SearchMatchInsights';

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: (_key: string, options?: { defaultValue?: string }) => options?.defaultValue ?? _key,
    }),
}));

function createLead(params: {
    leadKey: string;
    sourceName: string;
    scoreValue: number;
    scoreCategory: string;
    websitePresence: LeadResult['candidate']['websitePresence'];
}): LeadResult {
    return {
        leadKey: params.leadKey,
        candidate: {
            name: `Company ${params.leadKey}`,
            website: params.websitePresence === 'HAS_WEBSITE' ? `https://${params.leadKey}.example` : null,
            industry: 'Software',
            description: null,
            size: 'SMALL',
            location: 'Sao Paulo',
            contacts: [],
            websitePresence: params.websitePresence,
        },
        score: {
            value: params.scoreValue,
            category: params.scoreCategory,
            reasoning: 'Match reasoning',
        },
        source: {
            sourceName: params.sourceName,
            sourceUrl: null,
            collectedAt: '2026-03-16T10:00:00Z',
        },
    };
}

describe('SearchMatchInsights', () => {
    it('renders source breakdown and website presence distribution from displayed leads', () => {
        const leads: LeadResult[] = [
            createLead({ leadKey: 'a', sourceName: 'in-memory', scoreValue: 90, scoreCategory: 'HOT', websitePresence: 'HAS_WEBSITE' }),
            createLead({ leadKey: 'b', sourceName: 'in-memory', scoreValue: 80, scoreCategory: 'HOT', websitePresence: 'NO_WEBSITE' }),
            createLead({ leadKey: 'c', sourceName: 'vector-company', scoreValue: 60, scoreCategory: 'HOT', websitePresence: 'UNKNOWN' }),
        ];

        const markup = renderToStaticMarkup(createElement(SearchMatchInsights, { leads, selectedIcpName: 'SaaS Growth' }));

        expect(markup).toContain('Match insights');
        expect(markup).toContain('ICP profile');
        expect(markup).toContain('SaaS Growth');
        expect(markup).toContain('Match distribution');
        expect(markup).toContain('Website presence');
        expect(markup).toContain('Com site');
        expect(markup).toContain('Sem site');
        expect(markup).toContain('Desconhecido');
        expect(markup).toContain('Leads');
        expect(markup).toContain('Average score');
        expect(markup).toContain('76.7');
        expect(markup).toContain('in-memory');
        expect(markup).toContain('vector-company');
        expect(markup).toContain('2/0/0');
        expect(markup).toContain('1/0/0');
        expect(markup.match(/1 \(33%\)/g) ?? []).toHaveLength(3);
    });

    it('returns no markup when there are no leads', () => {
        const markup = renderToStaticMarkup(createElement(SearchMatchInsights, { leads: [] }));

        expect(markup).toBe('');
    });
});
