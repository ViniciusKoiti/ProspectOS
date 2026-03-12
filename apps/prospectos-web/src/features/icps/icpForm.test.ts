import { describe, expect, it } from 'vitest';

import { createIcpFormDefaultValues, icpFormSchema, toIcpUpsert } from './icpForm';

describe('icpForm helpers', () => {
    it('normalizes multiline fields into an ICP upsert payload', () => {
        const parsed = icpFormSchema.parse({
            name: ' Revenue Ops ',
            description: ' B2B pipeline owners ',
            industriesText: 'SaaS, Fintech\nSaaS',
            regionsText: 'Brazil\nLATAM',
            targetRolesText: 'RevOps; COO',
            interestTheme: ' Pipeline reliability ',
            targetTechnologiesText: 'HubSpot\nSalesforce',
            minEmployeeCount: '10',
            maxEmployeeCount: '200',
        });

        expect(toIcpUpsert(parsed)).toEqual({
            name: 'Revenue Ops',
            description: 'B2B pipeline owners',
            industries: ['SaaS', 'Fintech'],
            regions: ['Brazil', 'LATAM'],
            targetRoles: ['RevOps', 'COO'],
            interestTheme: 'Pipeline reliability',
            targetTechnologies: ['HubSpot', 'Salesforce'],
            minEmployeeCount: 10,
            maxEmployeeCount: 200,
        });
    });

    it('hydrates default values from an existing ICP', () => {
        expect(
            createIcpFormDefaultValues({
                id: 7,
                name: 'Platform Teams',
                description: null,
                targetIndustries: ['Software', 'AI'],
                regions: ['Brazil'],
                targetTechnologies: ['Kubernetes', 'AWS'],
                minEmployeeCount: null,
                maxEmployeeCount: 500,
                targetRoles: ['CTO', 'VP Engineering'],
                interestTheme: 'Modernization',
            })
        ).toEqual({
            name: 'Platform Teams',
            description: '',
            industriesText: 'Software\nAI',
            regionsText: 'Brazil',
            targetRolesText: 'CTO\nVP Engineering',
            interestTheme: 'Modernization',
            targetTechnologiesText: 'Kubernetes\nAWS',
            minEmployeeCount: '',
            maxEmployeeCount: 500,
        });
    });

    it('rejects employee ranges where max is lower than min', () => {
        expect(() =>
            icpFormSchema.parse({
                name: 'Ops Leaders',
                description: '',
                industriesText: '',
                regionsText: '',
                targetRolesText: '',
                interestTheme: '',
                targetTechnologiesText: '',
                minEmployeeCount: '300',
                maxEmployeeCount: '100',
            })
        ).toThrow('Maximum employee count must be greater than or equal to minimum employee count');
    });
});
