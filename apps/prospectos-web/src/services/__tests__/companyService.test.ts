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

    it('requests company list with filters and pagination params', async () => {
        vi.mocked(api.get).mockResolvedValue({
            data: {
                items: [
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
                page: 1,
                size: 20,
                totalItems: 37,
                totalPages: 2,
            },
        });

        const result = await listCompanies({
            query: 'alpha',
            industry: 'Software',
            location: 'Sao Paulo',
            minScore: 70,
            maxScore: 95,
            hasContact: true,
            page: 1,
            size: 20,
        });

        expect(api.get).toHaveBeenCalledWith('/companies', {
            params: {
                query: 'alpha',
                industry: 'Software',
                location: 'Sao Paulo',
                minScore: 70,
                maxScore: 95,
                hasContact: true,
                page: 1,
                size: 20,
            },
        });
        expect(result.items).toHaveLength(1);
        expect(result.items[0].id).toBe('101');
        expect(result.items[0].score?.value).toBe(82);
        expect(result.items[0].primaryContactEmail).toBe('ceo@alpha.example');
        expect(result.items[0].contactCount).toBe(3);
        expect(result.page).toBe(1);
        expect(result.size).toBe(20);
        expect(result.totalItems).toBe(37);
        expect(result.totalPages).toBe(2);
    });

    it('normalizes legacy array response into paged shape', async () => {
        vi.mocked(api.get).mockResolvedValue({
            data: [
                {
                    id: 201,
                    name: 'Legacy Co',
                    industry: 'Services',
                    website: null,
                    description: null,
                    employeeCount: null,
                    location: 'Curitiba',
                    score: null,
                },
            ],
        });

        const result = await listCompanies({ page: 0, size: 10 });

        expect(result.items).toHaveLength(1);
        expect(result.items[0].id).toBe('201');
        expect(result.page).toBe(0);
        expect(result.size).toBe(10);
        expect(result.totalItems).toBe(1);
        expect(result.totalPages).toBe(1);
    });

    it('uses X-Total-Count header for legacy array pagination metadata', async () => {
        vi.mocked(api.get).mockResolvedValue({
            data: [
                {
                    id: 301,
                    name: 'Header Co',
                    industry: 'Software',
                    website: null,
                    description: null,
                    employeeCount: null,
                    location: 'Recife',
                    score: null,
                },
            ],
            headers: {
                'X-Total-Count': '42',
            },
        });

        const result = await listCompanies({ page: 1, size: 10 });

        expect(result.items).toHaveLength(1);
        expect(result.page).toBe(1);
        expect(result.size).toBe(10);
        expect(result.totalItems).toBe(42);
        expect(result.totalPages).toBe(5);
    });

    it('uses X-Total-Count header when paged payload omits total metadata', async () => {
        vi.mocked(api.get).mockResolvedValue({
            data: {
                items: [
                    {
                        id: 302,
                        name: 'Header Paged Co',
                        industry: 'Services',
                        website: null,
                        description: null,
                        employeeCount: null,
                        location: 'Fortaleza',
                        score: null,
                    },
                ],
                page: 1,
                size: 10,
            },
            headers: {
                'X-Total-Count': '21',
            },
        });

        const result = await listCompanies({ page: 1, size: 10 });

        expect(result.items).toHaveLength(1);
        expect(result.page).toBe(1);
        expect(result.size).toBe(10);
        expect(result.totalItems).toBe(21);
        expect(result.totalPages).toBe(3);
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
