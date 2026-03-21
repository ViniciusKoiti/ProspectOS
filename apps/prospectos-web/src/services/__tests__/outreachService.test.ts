import { beforeEach, describe, expect, it, vi } from 'vitest';

import { api } from '../api';
import { startOutreachCampaign } from '../outreachService';

vi.mock('../api', () => ({
    api: {
        post: vi.fn(),
    },
}));

describe('outreachService contract', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('starts outreach campaign using typed payload and parses backend response', async () => {
        vi.mocked(api.post).mockResolvedValue({
            data: {
                campaignId: 91,
                segment: 'NO_WEBSITE',
                sent: 1,
                failures: 1,
                responses: 0,
                leads: [
                    {
                        companyId: 101,
                        companyName: 'Alpha Systems',
                        website: null,
                        status: 'SENT',
                        message: 'Email queued',
                    },
                    {
                        companyId: 102,
                        companyName: 'Beta Commerce',
                        website: null,
                        status: 'FAILED',
                        message: 'Mailbox unavailable',
                    },
                ],
            },
        });

        const result = await startOutreachCampaign({
            segment: 'NO_WEBSITE',
            limit: 25,
        });

        expect(api.post).toHaveBeenCalledWith('/outreach/campaigns', {
            segment: 'NO_WEBSITE',
            limit: 25,
        });
        expect(result.campaignId).toBe('91');
        expect(result.segment).toBe('NO_WEBSITE');
        expect(result.summary.sent).toBe(1);
        expect(result.leads[1].status).toBe('FAILED');
    });

    it('derives summary from leads when backend omits counters', async () => {
        vi.mocked(api.post).mockResolvedValue({
            data: {
                campaignId: 'cmp-22',
                segment: 'HAS_WEBSITE',
                leads: [
                    {
                        leadId: 'lead-1',
                        companyName: 'Gamma Labs',
                        website: 'https://gamma.example',
                        status: 'REPLIED',
                        detail: null,
                    },
                    {
                        leadId: 'lead-2',
                        companyName: 'Delta HQ',
                        website: 'https://delta.example',
                        status: 'SENT',
                        detail: null,
                    },
                    {
                        leadId: 'lead-3',
                        companyName: 'Epsilon Works',
                        website: 'https://epsilon.example',
                        status: 'FAILED',
                        detail: 'Provider timeout',
                    },
                ],
            },
        });

        const result = await startOutreachCampaign({
            segment: 'HAS_WEBSITE',
            limit: 50,
        });

        expect(result.summary).toEqual({
            sent: 1,
            failed: 1,
            replied: 1,
            total: 3,
        });
    });
});
