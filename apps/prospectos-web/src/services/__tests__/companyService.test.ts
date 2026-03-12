import { beforeEach, describe, expect, it, vi } from 'vitest';

import { api } from '../api';
import { getCompany, listCompanies } from '../companyService';

vi.mock('../api', () => ({
    api: {
        get: vi.fn(),
    },
}));

describe('companyService contract', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('parses company list response', async () => {
        vi.mocked(api.get).mockResolvedValue({
            data: [
                {
                    id: 101,
                    name: 'Alpha Systems',
                    industry: 'Software',
                    website: 'https://alpha.example',
                    description: 'Platform vendor',
                    employeeCount: 120,
                    location: 'Sao Paulo',
                    score: {
                        value: 82,
                        category: 'HOT',
                        reasoning: 'Good fit',
                    },
                },
            ],
        });

        const result = await listCompanies();

        expect(result).toHaveLength(1);
        expect(result[0].score?.value).toBe(82);
    });

    it('parses company detail response', async () => {
        vi.mocked(api.get).mockResolvedValue({
            data: {
                id: 102,
                name: 'Nexus Health',
                industry: 'Health Tech',
                website: 'https://nexus.example',
                description: 'Healthcare software',
                employeeCount: 80,
                location: 'Curitiba',
                score: {
                    value: 74,
                    category: 'WARM',
                    reasoning: 'Possible fit',
                },
            },
        });

        const result = await getCompany(102);

        expect(result.name).toBe('Nexus Health');
        expect(api.get).toHaveBeenCalledWith('/companies/102');
    });
});
