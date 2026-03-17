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
    'common.loading': 'Carregando',
    'ui.error.title': 'Algo deu errado',
    'ui.loading.label': 'Carregando dados',
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

type CompaniesPageData = {
    items: Company[];
    page: number;
    size: number;
    totalItems: number;
    totalPages: number;
};

type CompaniesQueryState = {
    data?: CompaniesPageData;
    isLoading: boolean;
    isError: boolean;
    isFetching?: boolean;
};

function mockCompaniesQuery(state: CompaniesQueryState) {
    vi.mocked(useQuery).mockReturnValue({
        data: state.data,
        isLoading: state.isLoading,
        isError: state.isError,
        isFetching: state.isFetching ?? false,
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

function createCompany(index: number): Company {
    return {
        id: String(index),
        name: `Empresa ${index}`,
        industry: 'Software',
        website: null,
        description: null,
        employeeCount: 100 + index,
        location: 'Sao Paulo',
        score: index % 2 === 0
            ? {
                value: 60 + index,
                category: 'WARM',
                reasoning: 'Fit razoavel',
            }
            : null,
        primaryContactEmail: index % 2 === 0 ? `contato${index}@empresa.example` : null,
        contactCount: index % 2 === 0 ? 1 : 0,
    };
}

function createPageData(items: Company[], page: number, size: number, totalItems: number, totalPages: number): CompaniesPageData {
    return {
        items,
        page,
        size,
        totalItems,
        totalPages,
    };
}

describe('CompaniesPage', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('renders advanced filters and pagination indicators from backend metadata', () => {
        const secondPageItems = Array.from({ length: 5 }, (_, index) => createCompany(index + 6));

        mockCompaniesQuery({
            isLoading: false,
            isError: false,
            data: createPageData(secondPageItems, 1, 5, 12, 3),
        });

        const markup = renderPage();

        expect(markup).toContain('Filtros');
        expect(markup).toContain('Score maximo');
        expect(markup).toContain('Com contato');
        expect(markup).toContain('Mostrando 6-10 de 12 empresas');
        expect(markup).toContain('Pagina 2 de 3');
        expect(markup).toContain('Empresa 10');
        expect(markup).not.toContain('Empresa 11');
    });

    it('renders loading state while query is pending', () => {
        mockCompaniesQuery({
            isLoading: true,
            isError: false,
            data: undefined,
        });

        const markup = renderPage();

        expect(markup).toContain('Carregando dados');
    });
    it('keeps page visible and shows table-level loading while refreshing filtered results', () => {
        mockCompaniesQuery({
            isLoading: true,
            isError: false,
            isFetching: true,
            data: createPageData([createCompany(1)], 0, 10, 1, 1),
        });

        const markup = renderPage();

        expect(markup).toContain('data-testid="companies-page"');
        expect(markup).toContain('data-testid="companies-table-refreshing"');
        expect(markup).toContain('Empresa 1');
    });

    it('renders empty state when there are no companies', () => {
        mockCompaniesQuery({
            isLoading: false,
            isError: false,
            data: createPageData([], 0, 10, 0, 1),
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
