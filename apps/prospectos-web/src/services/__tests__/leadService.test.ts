import { beforeEach, describe, expect, it, vi } from 'vitest';

import { api } from '../api';
import { acceptLead, searchLeads } from '../leadService';

vi.mock('../api', () => ({
    api: {
        post: vi.fn(),
    },
}));

describe('leadService contract', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('parses lead search response', async () => {
        vi.mocked(api.post).mockResolvedValue({
            data: {
                status: 'COMPLETED',
                leads: [
                    {
                        candidate: {
                            name: 'Alpha Systems',
                            website: 'https://alpha.example',
                            industry: 'Software',
                            description: 'Platform vendor',
                            size: 'MEDIUM',
                            location: 'Sao Paulo',
                            contacts: ['hello@alpha.example'],
                        },
                        score: {
                            value: 82,
                            category: 'HOT',
                            reasoning: 'Good fit',
                        },
                        source: {
                            sourceName: 'in-memory',
                            sourceUrl: 'https://example.test',
                            collectedAt: '2026-03-11T12:00:00Z',
                        },
                        leadKey: 'lead-key-1',
                    },
                ],
                requestId: '7e17bb6b-ec6a-47c6-b4a8-f6da4be2ebed',
                message: 'Completed',
            },
        });

        const result = await searchLeads({
            query: 'CTO fintech',
            limit: 20,
            sources: ['in-memory'],
            icpId: 1,
        });

        expect(result.status).toBe('COMPLETED');
        expect(result.leads[0].candidate.name).toBe('Alpha Systems');
        expect(api.post).toHaveBeenCalledWith('/leads/search', {
            query: 'CTO fintech',
            limit: 20,
            sources: ['in-memory'],
            icpId: 1,
        });
    });

    it('rejects invalid lead search request payloads before request', async () => {
        await expect(
            searchLeads({
                query: '',
                limit: 0,
                sources: [],
                icpId: null,
            })
        ).rejects.toThrow();
    });

    it('parses accept lead request and response contracts', async () => {
        vi.mocked(api.post).mockResolvedValue({
            data: {
                company: {
                    id: 12,
                    name: 'Alpha Systems',
                    industry: 'Software',
                    website: 'https://alpha.example',
                    description: 'Platform vendor',
                    employeeCount: null,
                    location: null,
                    score: {
                        value: 82,
                        category: 'HOT',
                        reasoning: 'Good fit',
                    },
                },
                message: 'Lead accepted and created',
            },
        });

        const result = await acceptLead({
            leadKey: 'lead-key-1',
            candidate: {
                name: 'Alpha Systems',
                website: 'https://alpha.example',
                industry: 'Software',
                description: 'Platform vendor',
                size: 'MEDIUM',
                location: 'Sao Paulo',
                contacts: ['hello@alpha.example'],
            },
            score: {
                value: 82,
                category: 'HOT',
                reasoning: 'Good fit',
            },
            source: {
                sourceName: 'in-memory',
                sourceUrl: 'https://example.test',
                collectedAt: '2026-03-11T12:00:00Z',
            },
        });

        expect(api.post).toHaveBeenCalledWith('/leads/accept', expect.objectContaining({ leadKey: 'lead-key-1' }));
        expect(result.company.id).toBe('12');
    });
});

