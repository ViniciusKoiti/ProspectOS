import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { createElement } from 'react';
import { renderToStaticMarkup } from 'react-dom/server';
import { useForm } from 'react-hook-form';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import type { LeadResult } from '../../types/leadContracts';
import SearchPage from '../SearchPage';

vi.mock('@tanstack/react-query', () => ({
    useMutation: vi.fn(),
    useQuery: vi.fn(),
    useQueryClient: vi.fn(),
}));

vi.mock('react-hook-form', () => ({
    useForm: vi.fn(),
}));

const translations: Record<string, string> = {
    'pages.search.title': 'Busca',
    'pages.search.description': 'Busca com IA',
    'pages.search.fields.query': 'Query',
    'pages.search.fields.icp': 'ICP',
    'pages.search.fields.limit': 'Limite',
    'pages.search.fields.sources': 'Sources',
    'pages.search.fields.sourcesHint': 'Selecione uma ou mais fontes.',
    'pages.search.fields.websitePresence': 'Website presence',
    'pages.search.placeholders.query': 'Digite o prompt',
    'pages.search.placeholders.selectIcp': 'Selecione ICP',
    'pages.search.websitePresence.all': 'Todos',
    'pages.search.websitePresence.hasWebsite': 'Com site',
    'pages.search.websitePresence.noWebsite': 'Sem site',
    'pages.search.websitePresence.unknown': 'Desconhecido',
    'pages.search.errors.loadIcps': 'Falha ao carregar ICPs',
    'pages.search.errors.execute': 'Falha ao executar busca.',
    'pages.search.errors.accept': 'Falha ao aceitar lead.',
    'pages.search.empty.title': 'Sem resultados',
    'pages.search.empty.description': 'Execute uma busca para ver resultados.',
    'pages.search.table.source': 'Source',
    'pages.search.actions.accept': 'Aceitar',
    'pages.search.actions.viewCompany': 'Ver empresa',
    'pages.search.actions.exportCsv': 'Export CSV',
    'pages.search.feedback.acceptSuccess': 'Lead aceito com sucesso',
    'common.actions': 'Ações',
    'common.retry': 'Tentar novamente',
    'common.loading': 'Carregando',
    'common.searchProspects': 'Buscar Prospects',
    'ui.error.title': 'Algo deu errado',
    'ui.loading.label': 'Carregando dados',
    'pages.companies.table.company': 'Empresa',
    'pages.companies.table.industry': 'Industria',
    'pages.companies.table.score': 'Score',
};

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: (key: string, options?: { defaultValue?: string }) => options?.defaultValue ?? translations[key] ?? key,
    }),
}));

type MutationState = {
    isPending: boolean;
    isError: boolean;
    error: unknown;
    data: unknown;
};

function createLead(params: { leadKey: string; websitePresence: LeadResult['candidate']['websitePresence']; website: string | null }): LeadResult {
    return {
        leadKey: params.leadKey,
        candidate: {
            name: `Company ${params.leadKey}`,
            website: params.website,
            websitePresence: params.websitePresence,
            industry: 'Software',
            description: null,
            size: 'MEDIUM',
            location: 'Sao Paulo',
            contacts: ['hello@alpha.example'],
        },
        score: {
            value: 88,
            category: 'HOT',
            reasoning: 'Strong fit',
        },
        source: {
            sourceName: 'in-memory',
            sourceUrl: null,
            collectedAt: '2026-03-16T10:00:00Z',
        },
    };
}

function mockForm(websitePresence: 'all' | 'HAS_WEBSITE' | 'NO_WEBSITE' = 'all') {
    vi.mocked(useForm).mockReturnValue({
        register: vi.fn(() => ({})),
        watch: vi.fn((field: string) => {
            if (field === 'sources') {
                return ['in-memory'];
            }

            if (field === 'icpId') {
                return null;
            }

            if (field === 'websitePresence') {
                return websitePresence;
            }

            return undefined;
        }),
        setValue: vi.fn(),
        handleSubmit: vi.fn((callback) => () => callback({
            query: 'empresas de software',
            limit: 20,
            icpId: null,
            sources: ['in-memory'],
            websitePresence,
        })),
        formState: {
            errors: {},
        },
    } as unknown as ReturnType<typeof useForm>);
}

function mockPageState(params: {
    icpsLoading?: boolean;
    icpsError?: boolean;
    search: MutationState;
    accept?: Partial<MutationState>;
    websitePresence?: 'all' | 'HAS_WEBSITE' | 'NO_WEBSITE';
}) {
    vi.mocked(useQueryClient).mockReturnValue({
        invalidateQueries: vi.fn().mockResolvedValue(undefined),
    } as unknown as ReturnType<typeof useQueryClient>);

    vi.mocked(useQuery).mockReturnValue({
        isLoading: params.icpsLoading ?? false,
        isError: params.icpsError ?? false,
        data: [{ id: '1', name: 'ICP SaaS' }],
        refetch: vi.fn(),
    } as unknown as ReturnType<typeof useQuery>);

    const searchMutation = {
        ...params.search,
        mutateAsync: vi.fn(),
        reset: vi.fn(),
    };

    const acceptMutation = {
        isPending: false,
        isError: false,
        error: null,
        data: null,
        mutateAsync: vi.fn(),
        reset: vi.fn(),
        ...params.accept,
    };

    vi.mocked(useMutation)
        .mockReturnValueOnce(searchMutation as unknown as ReturnType<typeof useMutation>)
        .mockReturnValueOnce(acceptMutation as unknown as ReturnType<typeof useMutation>);

    mockForm(params.websitePresence);
}

function renderPage() {
    return renderToStaticMarkup(
        createElement(
            MemoryRouter,
            null,
            createElement(SearchPage),
        )
    );
}

describe('SearchPage', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('renders processing state message when search status is PROCESSING', () => {
        mockPageState({
            search: {
                isPending: false,
                isError: false,
                error: null,
                data: {
                    status: 'PROCESSING',
                    leads: [],
                    requestId: 'ed52a39f-51cc-4a37-a6cf-bce4f7a5469a',
                    message: 'Processando busca no provedor...',
                },
            },
        });

        const markup = renderPage();

        expect(markup).toContain('Processando busca no provedor...');
    });

    it('renders failed state message and retry action when search status is FAILED', () => {
        mockPageState({
            search: {
                isPending: false,
                isError: false,
                error: null,
                data: {
                    status: 'FAILED',
                    leads: [],
                    requestId: '8bda7b48-c187-4209-8775-a2dfce18ff6f',
                    message: 'Falha no provider externo',
                },
            },
        });

        const markup = renderPage();

        expect(markup).toContain('Algo deu errado');
        expect(markup).toContain('Falha no provider externo');
        expect(markup).toContain('Tentar novamente');
    });

    it('merges fallback and api error details when mutation fails', () => {
        mockPageState({
            search: {
                isPending: false,
                isError: true,
                error: {
                    isAxiosError: true,
                    response: {
                        status: 503,
                        data: {
                            message: 'Backend indisponível temporariamente',
                        },
                    },
                    message: 'Request failed',
                },
                data: null,
            },
        });

        const markup = renderPage();

        expect(markup).toContain('Falha ao executar busca. Backend indisponível temporariamente');
    });

    it('renders result table, website presence badges and export action when completed', () => {
        mockPageState({
            search: {
                isPending: false,
                isError: false,
                error: null,
                data: {
                    status: 'COMPLETED',
                    leads: [
                        createLead({ leadKey: 'lead-1', websitePresence: 'HAS_WEBSITE', website: 'https://alpha.example' }),
                        createLead({ leadKey: 'lead-2', websitePresence: 'NO_WEBSITE', website: null }),
                    ],
                    requestId: '9fc98753-f486-42f7-b6eb-354ed4f4f0cc',
                    message: 'Busca concluída',
                },
            },
        });

        const markup = renderPage();

        expect(markup).toContain('search-results-table');
        expect(markup).toContain('Website presence');
        expect(markup).toContain('Com site');
        expect(markup).toContain('Sem site');
        expect(markup).toContain('Export CSV');
    });

    it('renders specific empty state when Sem site filter has no matches', () => {
        mockPageState({
            websitePresence: 'NO_WEBSITE',
            search: {
                isPending: false,
                isError: false,
                error: null,
                data: {
                    status: 'COMPLETED',
                    leads: [
                        createLead({ leadKey: 'lead-1', websitePresence: 'HAS_WEBSITE', website: 'https://alpha.example' }),
                    ],
                    requestId: '617825f9-a0ca-4f0b-b764-fad0f5b77638',
                    message: 'Busca concluida',
                },
            },
        });

        const markup = renderPage();

        expect(markup).toContain('Nenhum lead sem site encontrado');
        expect(markup).toContain('Ajuste os filtros ou execute uma nova busca para encontrar empresas sem site.');
    });

    it('filters displayed leads by website presence selection', () => {
        mockPageState({
            websitePresence: 'NO_WEBSITE',
            search: {
                isPending: false,
                isError: false,
                error: null,
                data: {
                    status: 'COMPLETED',
                    leads: [
                        createLead({ leadKey: 'lead-1', websitePresence: 'HAS_WEBSITE', website: 'https://alpha.example' }),
                        createLead({ leadKey: 'lead-2', websitePresence: 'NO_WEBSITE', website: null }),
                    ],
                    requestId: '33ab1cc6-3a1d-4be0-9aa9-f0d7a6d9f7f6',
                    message: 'Busca concluída',
                },
            },
        });

        const markup = renderPage();

        expect(markup).not.toContain('Company lead-1');
        expect(markup).toContain('Company lead-2');
    });
});
