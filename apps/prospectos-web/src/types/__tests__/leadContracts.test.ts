import { describe, expect, it } from 'vitest';

import { leadSearchResponseSchema } from '../leadContracts';

describe('leadContracts', () => {
    it('derives websitePresence from legacy payload without the field', () => {
        const parsed = leadSearchResponseSchema.parse({
            status: 'COMPLETED',
            leads: [
                {
                    leadKey: 'lead-1',
                    candidate: {
                        name: 'Alpha Systems',
                        website: 'https://alpha.example',
                        industry: 'Software',
                        description: null,
                        size: 'MEDIUM',
                        location: 'Sao Paulo',
                        contacts: [],
                    },
                    score: {
                        value: 91,
                        category: 'HOT',
                        reasoning: 'Strong fit',
                    },
                    source: {
                        sourceName: 'in-memory',
                        sourceUrl: null,
                        collectedAt: '2026-03-16T10:00:00Z',
                    },
                },
                {
                    leadKey: 'lead-2',
                    candidate: {
                        name: 'Beta Labs',
                        website: null,
                        industry: 'Services',
                        description: null,
                        size: 'SMALL',
                        location: 'Rio de Janeiro',
                        contacts: [],
                    },
                    score: {
                        value: 70,
                        category: 'WARM',
                        reasoning: 'Partial fit',
                    },
                    source: {
                        sourceName: 'vector-company',
                        sourceUrl: null,
                        collectedAt: '2026-03-16T10:00:00Z',
                    },
                },
            ],
            requestId: 'c0f9f61b-0f8d-4a7e-b72d-5a81e5ad0f9b',
            message: null,
        });

        expect(parsed.leads[0].candidate.websitePresence).toBe('HAS_WEBSITE');
        expect(parsed.leads[1].candidate.websitePresence).toBe('NO_WEBSITE');
    });

    it('preserves explicit unknown websitePresence from payloads that already send it', () => {
        const parsed = leadSearchResponseSchema.parse({
            status: 'COMPLETED',
            leads: [
                {
                    leadKey: 'lead-3',
                    candidate: {
                        name: 'Gamma Co',
                        website: null,
                        websitePresence: 'UNKNOWN',
                        industry: null,
                        description: null,
                        size: null,
                        location: null,
                        contacts: [],
                    },
                    score: {
                        value: 55,
                        category: 'COLD',
                        reasoning: 'Low fit',
                    },
                    source: {
                        sourceName: 'in-memory',
                        sourceUrl: null,
                        collectedAt: '2026-03-16T10:00:00Z',
                    },
                },
            ],
            requestId: 'd4b5f1bb-0c77-4b5d-a30d-47b059fa4b0d',
            message: 'done',
        });

        expect(parsed.leads[0].candidate.websitePresence).toBe('UNKNOWN');
    });
});
