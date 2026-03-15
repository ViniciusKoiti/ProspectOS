import { beforeEach, describe, expect, it, vi } from 'vitest';

import { api } from '../api';
import { getCompany, getCompanyContacts, listCompanies } from '../companyService';

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
                    primaryContactEmail: 'ceo@alpha.example',
                    contactCount: 3,
                },
            ],
        });

        const result = await listCompanies();

        expect(result).toHaveLength(1);
        expect(result[0].id).toBe('101');
        expect(result[0].score?.value).toBe(82);
        expect(result[0].primaryContactEmail).toBe('ceo@alpha.example');
        expect(result[0].contactCount).toBe(3);
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

        const result = await getCompany('102');

        expect(result.name).toBe('Nexus Health');
        expect(result.primaryContactEmail).toBeNull();
        expect(result.contactCount).toBe(0);
        expect(api.get).toHaveBeenCalledWith('/companies/102');
    });

    it('parses company contacts response', async () => {
        vi.mocked(api.get).mockResolvedValue({
            data: [
                {
                    name: 'Maria Silva',
                    email: 'maria@alpha.example',
                    position: 'CTO',
                    phoneNumber: '+55 11 99999-9999',
                },
            ],
        });

        const result = await getCompanyContacts('101');

        expect(result).toHaveLength(1);
        expect(result[0]).toEqual({
            name: 'Maria Silva',
            email: 'maria@alpha.example',
            position: 'CTO',
            phoneNumber: '+55 11 99999-9999',
        });
        expect(api.get).toHaveBeenCalledWith('/companies/101/contacts');
    });
});
