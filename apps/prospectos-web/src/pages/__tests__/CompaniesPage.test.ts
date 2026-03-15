import { useQuery } from '@tanstack/react-query';
import { createElement } from 'react';
import { renderToStaticMarkup } from 'react-dom/server';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import type { Company } from '../../types/companyContracts';
import CompaniesPage from '../CompaniesPage';

vi.mock('@tanstack/react-query', () => ({
    useQuery: vi.fn(),
}));

vi.mock('../../services/companyService', () => ({
    listCompanies: vi.fn(),
}));

const translations: Record<string, string> = {
    'common.retry': 'Tentar novamente',
    'ui.error.title': 'Algo deu errado',
    'pages.companies.title': 'Empresas',
    'pages.companies.description': 'Primitivos consistentes de tabela facilitam a evolucao de listados.',
    'pages.companies.table.company': 'Empresa',
    'pages.companies.table.industry': 'Industria',
    'pages.companies.table.primaryContact': 'Contato principal',
    'pages.companies.table.contactCount': '{{count}} contatos',
    'pages.companies.table.noContact': 'Sem contato',
    'pages.companies.table.score': 'Score',
    'pages.companies.empty.title': 'Nenhuma empresa disponivel',
    'pages.companies.empty.description': 'Leads aceitos aparecerao aqui apos integracao dos fluxos.',
    'pages.companies.errors.load': 'Falha ao carregar empresas.',
};

function translate(key: string, options?: { count?: number }): string {
    const template = translations[key] ?? key;
    if (typeof options?.count === 'number') {
        return template.replace('{{count}}', String(options.count));
    }

    return template;
}

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: translate,
    }),
}));

type CompaniesQueryState = {
    data?: Company[];
    isLoading: boolean;
    isError: boolean;
};

function mockCompaniesQuery(state: CompaniesQueryState) {
    vi.mocked(useQuery).mockReturnValue({
        data: state.data,
        isLoading: state.isLoading,
        isError: state.isError,
        refetch: () => Promise.resolve(),
    } as unknown as ReturnType<typeof useQuery>);
}

function renderPage() {
    return renderToStaticMarkup(
        createElement(
            MemoryRouter,
            null,
            createElement(CompaniesPage),
        ),
    );
}

describe('CompaniesPage', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('renders companies table with primary contact summary', () => {
        mockCompaniesQuery({
            isLoading: false,
            isError: false,
            data: [
                {
                    id: '42',
                    name: 'Alpha Systems',
                    industry: 'Software',
                    website: null,
                    description: null,
                    employeeCount: 120,
                    location: 'Sao Paulo',
                    score: null,
                    primaryContactEmail: 'ceo@alpha.example',
                    contactCount: 2,
                },
            ],
        });

        const markup = renderPage();

        expect(markup).toContain('Contato principal');
        expect(markup).toContain('ceo@alpha.example');
        expect(markup).toContain('2 contatos');
    });

    it('renders empty state when there are no companies', () => {
        mockCompaniesQuery({
            isLoading: false,
            isError: false,
            data: [],
        });

        const markup = renderPage();

        expect(markup).toContain('Nenhuma empresa disponivel');
        expect(markup).toContain('Leads aceitos aparecerao aqui apos integracao dos fluxos.');
    });

    it('renders error state when query fails', () => {
        mockCompaniesQuery({
            isLoading: false,
            isError: true,
            data: undefined,
        });

        const markup = renderPage();

        expect(markup).toContain('Algo deu errado');
        expect(markup).toContain('Falha ao carregar empresas.');
        expect(markup).toContain('Tentar novamente');
    });
});
