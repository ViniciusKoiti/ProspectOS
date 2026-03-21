import { useMutation } from '@tanstack/react-query';
import { createElement } from 'react';
import { renderToStaticMarkup } from 'react-dom/server';
import { useForm } from 'react-hook-form';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import OutreachPage from '../OutreachPage';

vi.mock('@tanstack/react-query', () => ({
    useMutation: vi.fn(),
}));

vi.mock('react-hook-form', () => ({
    useForm: vi.fn(),
}));

const translations: Record<string, string> = {
    'common.retry': 'Tentar novamente',
    'common.loading': 'Carregando...',
    'ui.error.title': 'Algo deu errado',
};

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: (key: string, options?: { defaultValue?: string }) => options?.defaultValue ?? translations[key] ?? key,
    }),
}));

type OutreachFormValues = {
    segment: 'ALL' | 'HAS_WEBSITE' | 'NO_WEBSITE';
    limit: number;
};

type MutationState = {
    isPending: boolean;
    isError: boolean;
    error: unknown;
    data: {
        campaignId: string;
        segment: 'ALL' | 'HAS_WEBSITE' | 'NO_WEBSITE';
        summary: {
            sent: number;
            failed: number;
            replied: number;
            total: number;
        };
        leads: Array<{
            leadId: string;
            companyName: string;
            website: string | null;
            status: 'SENT' | 'FAILED' | 'REPLIED';
            detail: string | null;
        }>;
    } | null;
};

let submitHandler: ((values: OutreachFormValues) => Promise<void>) | null = null;

function mockForm() {
    vi.mocked(useForm).mockReturnValue({
        register: vi.fn(() => ({})),
        handleSubmit: vi.fn((callback) => {
            submitHandler = callback as (values: OutreachFormValues) => Promise<void>;
            return () => undefined;
        }),
        formState: {
            errors: {},
        },
    } as unknown as ReturnType<typeof useForm>);
}

function mockPageState(state: MutationState) {
    const mutateAsync = vi.fn().mockResolvedValue(state.data);

    vi.mocked(useMutation).mockReturnValue({
        ...state,
        mutateAsync,
    } as unknown as ReturnType<typeof useMutation>);

    mockForm();

    return mutateAsync;
}

function renderPage() {
    return renderToStaticMarkup(
        createElement(
            MemoryRouter,
            null,
            createElement(OutreachPage),
        ),
    );
}

describe('OutreachPage', () => {
    beforeEach(() => {
        submitHandler = null;
        vi.clearAllMocks();
    });

    it('starts outreach campaign action with form values', async () => {
        const mutateAsync = mockPageState({
            isPending: false,
            isError: false,
            error: null,
            data: null,
        });

        renderPage();

        expect(submitHandler).not.toBeNull();

        await submitHandler?.({
            segment: 'NO_WEBSITE',
            limit: 25,
        });

        expect(mutateAsync).toHaveBeenCalledWith({
            segment: 'NO_WEBSITE',
            limit: 25,
        });
    });

    it('renders status table and aggregated indicators for returned leads', () => {
        mockPageState({
            isPending: false,
            isError: false,
            error: null,
            data: {
                campaignId: 'cmp-9',
                segment: 'NO_WEBSITE',
                summary: {
                    sent: 2,
                    failed: 1,
                    replied: 1,
                    total: 4,
                },
                leads: [
                    {
                        leadId: 'lead-1',
                        companyName: 'Alpha',
                        website: 'https://alpha.example',
                        status: 'SENT',
                        detail: 'Delivered',
                    },
                    {
                        leadId: 'lead-2',
                        companyName: 'Beta',
                        website: null,
                        status: 'FAILED',
                        detail: 'Bounce',
                    },
                    {
                        leadId: 'lead-3',
                        companyName: 'Gamma',
                        website: null,
                        status: 'REPLIED',
                        detail: 'Answered',
                    },
                ],
            },
        });

        const markup = renderPage();

        expect(markup).toContain('data-testid="outreach-results-table"');
        expect(markup).toContain('Campanha #cmp-9');
        expect(markup).toContain('SENT');
        expect(markup).toContain('FAILED');
        expect(markup).toContain('REPLIED');
        expect(markup).toContain('data-testid="outreach-indicator-sent"');
        expect(markup).toContain('data-testid="outreach-indicator-failed"');
        expect(markup).toContain('data-testid="outreach-indicator-replied"');
        expect(markup).toContain('data-testid="outreach-indicator-total"');
        expect(markup).toContain('data-value="2"');
        expect(markup).toContain('data-value="1"');
        expect(markup).toContain('data-value="4"');
    });

    it('renders empty state when campaign response has no leads', () => {
        mockPageState({
            isPending: false,
            isError: false,
            error: null,
            data: {
                campaignId: 'cmp-empty',
                segment: 'ALL',
                summary: {
                    sent: 0,
                    failed: 0,
                    replied: 0,
                    total: 0,
                },
                leads: [],
            },
        });

        const markup = renderPage();

        expect(markup).toContain('data-testid="outreach-results-empty"');
        expect(markup).toContain('Nenhum lead elegivel no segmento selecionado');
        expect(markup).toContain('Ajuste o segmento ou aumente o limite para tentar novamente.');
    });
});
