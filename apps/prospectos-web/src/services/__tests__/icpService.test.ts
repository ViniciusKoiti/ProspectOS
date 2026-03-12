import { beforeEach, describe, expect, it, vi } from 'vitest';

import { api } from '../api';
import { createIcp, deleteIcp, listIcps, updateIcp } from '../icpService';

vi.mock('../api', () => ({
    api: {
        get: vi.fn(),
        post: vi.fn(),
        put: vi.fn(),
        delete: vi.fn(),
    },
}));

describe('icpService contract', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('parses list ICP response using backend contract', async () => {
        vi.mocked(api.get).mockResolvedValue({
            data: [
                {
                    id: 1,
                    name: 'Tech Decision Makers',
                    description: 'Profiles for technical buyers',
                    targetIndustries: ['Software'],
                    regions: ['Brazil'],
                    targetTechnologies: ['AWS'],
                    minEmployeeCount: 10,
                    maxEmployeeCount: 500,
                    targetRoles: ['CTO'],
                    interestTheme: 'Platform modernization',
                },
            ],
        });

        const result = await listIcps();

        expect(result[0].name).toBe('Tech Decision Makers');
        expect(result[0].targetIndustries).toEqual(['Software']);
    });

    it('rejects invalid ICP response payloads', async () => {
        vi.mocked(api.get).mockResolvedValue({
            data: [{ id: '1', name: 'Broken contract' }],
        });

        await expect(listIcps()).rejects.toThrow();
    });

    it('parses create ICP request and response contracts', async () => {
        vi.mocked(api.post).mockResolvedValue({
            data: {
                id: 3,
                name: 'Revenue Ops',
                description: 'B2B operations buyers',
                targetIndustries: ['SaaS'],
                regions: ['LATAM'],
                targetTechnologies: ['HubSpot'],
                minEmployeeCount: 20,
                maxEmployeeCount: 300,
                targetRoles: ['RevOps'],
                interestTheme: 'Pipeline reliability',
            },
        });

        const result = await createIcp({
            name: 'Revenue Ops',
            description: 'B2B operations buyers',
            industries: ['SaaS'],
            regions: ['LATAM'],
            targetRoles: ['RevOps'],
            interestTheme: 'Pipeline reliability',
            targetTechnologies: ['HubSpot'],
            minEmployeeCount: 20,
            maxEmployeeCount: 300,
        });

        expect(api.post).toHaveBeenCalledWith('/icps', expect.objectContaining({ name: 'Revenue Ops' }));
        expect(result.id).toBe(3);
    });

    it('parses update ICP request and response contracts', async () => {
        vi.mocked(api.put).mockResolvedValue({
            data: {
                id: 3,
                name: 'Revenue Ops Updated',
                description: 'Expanded segment',
                targetIndustries: ['SaaS'],
                regions: ['LATAM'],
                targetTechnologies: ['HubSpot'],
                minEmployeeCount: 50,
                maxEmployeeCount: 500,
                targetRoles: ['RevOps'],
                interestTheme: 'Forecast discipline',
            },
        });

        const result = await updateIcp(3, {
            name: 'Revenue Ops Updated',
            description: 'Expanded segment',
            industries: ['SaaS'],
            regions: ['LATAM'],
            targetRoles: ['RevOps'],
            interestTheme: 'Forecast discipline',
            targetTechnologies: ['HubSpot'],
            minEmployeeCount: 50,
            maxEmployeeCount: 500,
        });

        expect(api.put).toHaveBeenCalledWith('/icps/3', expect.objectContaining({ name: 'Revenue Ops Updated' }));
        expect(result.interestTheme).toBe('Forecast discipline');
    });

    it('calls delete ICP endpoint using the backend contract', async () => {
        vi.mocked(api.delete).mockResolvedValue({ data: null });

        await deleteIcp(9);

        expect(api.delete).toHaveBeenCalledWith('/icps/9');
    });
});
