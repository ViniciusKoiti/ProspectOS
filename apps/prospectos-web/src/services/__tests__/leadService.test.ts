import { beforeEach, describe, expect, it, vi } from 'vitest';

import { api } from '../api';
import { acceptLead, getLeadSearchAsyncStatus, searchLeads, startLeadSearchAsync } from '../leadService';

vi.mock('../api', () => ({
    api: {
        post: vi.fn(),
        get: vi.fn(),
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

    it('starts async lead search and parses accepted response', async () => {
        vi.mocked(api.post).mockResolvedValue({
            data: {
                requestId: '7e17bb6b-ec6a-47c6-b4a8-f6da4be2ebed',
                status: 'PROCESSING',
                message: 'Search started',
                createdAt: '2026-03-21T12:00:00Z',
            },
        });

        const result = await startLeadSearchAsync({
            query: 'agencias de marketing',
            limit: 10,
            sources: ['in-memory', 'cnpj-ws'],
            icpId: 1,
        });

        expect(result.status).toBe('PROCESSING');
        expect(api.post).toHaveBeenCalledWith('/leads/search/async', {
            query: 'agencias de marketing',
            limit: 10,
            sources: ['in-memory', 'cnpj-ws'],
            icpId: 1,
        });
    });

    it('parses async lead search snapshot response', async () => {
        vi.mocked(api.get).mockResolvedValue({
            data: {
                requestId: '7e17bb6b-ec6a-47c6-b4a8-f6da4be2ebed',
                status: 'PROCESSING',
                message: 'Running',
                progress: {
                    doneSources: 1,
                    totalSources: 2,
                    failedSources: 0,
                },
                sourceRuns: [
                    {
                        sourceName: 'in-memory',
                        status: 'COMPLETED',
                        durationMs: 125,
                        message: 'ok',
                    },
                    {
                        sourceName: 'cnpj-ws',
                        status: 'RUNNING',
                        durationMs: null,
                        message: null,
                    },
                ],
                leads: [],
                createdAt: '2026-03-21T12:00:00Z',
                updatedAt: '2026-03-21T12:00:05Z',
                completedAt: null,
            },
        });

        const result = await getLeadSearchAsyncStatus('7e17bb6b-ec6a-47c6-b4a8-f6da4be2ebed');

        expect(result.progress.doneSources).toBe(1);
        expect(result.sourceRuns[0].status).toBe('COMPLETED');
        expect(api.get).toHaveBeenCalledWith('/leads/search/7e17bb6b-ec6a-47c6-b4a8-f6da4be2ebed');
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
