import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation } from '@tanstack/react-query';
import { useMemo } from 'react';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import { z } from 'zod';

import Badge from '../components/ui/Badge';
import Button from '../components/ui/Button';
import Card from '../components/ui/Card';
import type { DataTableColumn } from '../components/ui/DataTable';
import DataTable from '../components/ui/DataTable';
import EmptyState from '../components/ui/EmptyState';
import ErrorState from '../components/ui/ErrorState';
import LoadingState from '../components/ui/LoadingState';
import PageHeader from '../components/ui/PageHeader';
import Select from '../components/ui/Select';
import { startOutreachCampaign } from '../services/outreachService';
import type { OutreachCampaignLead, OutreachSegment } from '../types/outreachContracts';
import { outreachSegmentSchema } from '../types/outreachContracts';
import { mergeWithFallbackError, parseApiErrorMessage } from './search/searchUtils';

const outreachFormSchema = z.object({
    segment: outreachSegmentSchema,
    limit: z.coerce.number().int().min(1).max(500),
});

type OutreachFormInput = z.input<typeof outreachFormSchema>;
type OutreachFormValues = z.output<typeof outreachFormSchema>;

type IndicatorItem = {
    testId: string;
    label: string;
    value: number;
};

function getStatusBadgeVariant(status: OutreachCampaignLead['status']): 'success' | 'warning' | 'neutral' {
    if (status === 'SENT') {
        return 'success';
    }

    if (status === 'FAILED') {
        return 'warning';
    }

    return 'neutral';
}

function getStatusLabel(status: OutreachCampaignLead['status'], t: (key: string, options?: { defaultValue?: string }) => string): string {
    if (status === 'SENT') {
        return t('pages.outreach.status.sent', { defaultValue: 'SENT' });
    }

    if (status === 'FAILED') {
        return t('pages.outreach.status.failed', { defaultValue: 'FAILED' });
    }

    return t('pages.outreach.status.replied', { defaultValue: 'REPLIED' });
}

function getSegmentLabel(segment: OutreachSegment, t: (key: string, options?: { defaultValue?: string }) => string): string {
    if (segment === 'HAS_WEBSITE') {
        return t('pages.outreach.segment.hasWebsite', { defaultValue: 'Com site' });
    }

    if (segment === 'NO_WEBSITE') {
        return t('pages.outreach.segment.noWebsite', { defaultValue: 'Sem site' });
    }

    return t('pages.outreach.segment.all', { defaultValue: 'Todos' });
}

export default function OutreachPage() {
    const { t } = useTranslation();

    const form = useForm<OutreachFormInput, unknown, OutreachFormValues>({
        resolver: zodResolver(outreachFormSchema),
        defaultValues: {
            segment: 'NO_WEBSITE',
            limit: 50,
        },
    });

    const campaignMutation = useMutation({ mutationFn: startOutreachCampaign });

    const indicators = useMemo<IndicatorItem[]>(() => {
        const summary = campaignMutation.data?.summary;

        if (!summary) {
            return [];
        }

        return [
            {
                testId: 'outreach-indicator-sent',
                label: t('pages.outreach.indicators.sent', { defaultValue: 'Enviados' }),
                value: summary.sent,
            },
            {
                testId: 'outreach-indicator-failed',
                label: t('pages.outreach.indicators.failed', { defaultValue: 'Falhas' }),
                value: summary.failed,
            },
            {
                testId: 'outreach-indicator-replied',
                label: t('pages.outreach.indicators.replied', { defaultValue: 'Respostas' }),
                value: summary.replied,
            },
            {
                testId: 'outreach-indicator-total',
                label: t('pages.outreach.indicators.total', { defaultValue: 'Total' }),
                value: summary.total,
            },
        ];
    }, [campaignMutation.data?.summary, t]);

    const errorMessage = campaignMutation.isError
        ? mergeWithFallbackError(
            t('pages.outreach.errors.execute', { defaultValue: 'Falha ao iniciar a campanha de outreach.' }),
            parseApiErrorMessage(campaignMutation.error)
        )
        : null;

    const columns: DataTableColumn<OutreachCampaignLead>[] = [
        {
            key: 'companyName',
            header: t('pages.outreach.table.company', { defaultValue: 'Empresa' }),
            render: (row) => row.companyName,
        },
        {
            key: 'website',
            header: t('pages.outreach.table.website', { defaultValue: 'Website' }),
            render: (row) => row.website ?? '-',
        },
        {
            key: 'status',
            header: t('pages.outreach.table.status', { defaultValue: 'Status' }),
            render: (row) => (
                <Badge variant={getStatusBadgeVariant(row.status)}>
                    {getStatusLabel(row.status, t)}
                </Badge>
            ),
        },
        {
            key: 'detail',
            header: t('pages.outreach.table.detail', { defaultValue: 'Detalhe' }),
            render: (row) => row.detail ?? '-',
        },
    ];

    const onSubmit = form.handleSubmit(async (values) => {
        await campaignMutation.mutateAsync(values);
    });

    return (
        <section className="space-y-4" data-testid="outreach-page">
            <PageHeader
                title={t('pages.outreach.title', { defaultValue: 'Outreach Email' })}
                description={t('pages.outreach.description', { defaultValue: 'Inicie uma campanha por segmento e acompanhe o status por lead.' })}
            />

            <div className="grid grid-cols-1 gap-4 lg:grid-cols-[1fr,1.2fr]">
                <Card>
                    <form className="space-y-4" onSubmit={onSubmit} data-testid="outreach-form">
                        <Select
                            id="outreach-segment"
                            label={t('pages.outreach.fields.segment', { defaultValue: 'Segmento' })}
                            error={form.formState.errors.segment?.message}
                            disabled={campaignMutation.isPending}
                            {...form.register('segment')}
                        >
                            <option value="NO_WEBSITE">{t('pages.outreach.segment.noWebsite', { defaultValue: 'Sem site' })}</option>
                            <option value="HAS_WEBSITE">{t('pages.outreach.segment.hasWebsite', { defaultValue: 'Com site' })}</option>
                            <option value="ALL">{t('pages.outreach.segment.all', { defaultValue: 'Todos' })}</option>
                        </Select>

                        <Select
                            id="outreach-limit"
                            label={t('pages.outreach.fields.limit', { defaultValue: 'Limite de leads' })}
                            error={form.formState.errors.limit?.message}
                            disabled={campaignMutation.isPending}
                            {...form.register('limit')}
                        >
                            <option value="10">10</option>
                            <option value="25">25</option>
                            <option value="50">50</option>
                            <option value="100">100</option>
                            <option value="250">250</option>
                        </Select>

                        <div className="flex justify-end">
                            <Button
                                type="submit"
                                loading={campaignMutation.isPending}
                                disabled={campaignMutation.isPending}
                                data-testid="outreach-start-button"
                            >
                                {t('pages.outreach.actions.startCampaign', { defaultValue: 'Iniciar campanha' })}
                            </Button>
                        </div>
                    </form>
                </Card>

                <div className="space-y-4">
                    {campaignMutation.isPending ? (
                        <LoadingState label={t('pages.outreach.loading', { defaultValue: 'Disparando campanha de outreach...' })} />
                    ) : errorMessage ? (
                        <ErrorState message={errorMessage} onRetry={() => void onSubmit()} />
                    ) : campaignMutation.data ? (
                        campaignMutation.data.leads.length === 0 ? (
                            <div data-testid="outreach-results-empty">
                                <EmptyState
                                    title={t('pages.outreach.empty.noLeadsTitle', { defaultValue: 'Nenhum lead elegivel no segmento selecionado' })}
                                    description={t('pages.outreach.empty.noLeadsDescription', { defaultValue: 'Ajuste o segmento ou aumente o limite para tentar novamente.' })}
                                />
                            </div>
                        ) : (
                            <div className="space-y-4" data-testid="outreach-results-table">
                                <div className="rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-700">
                                    <span className="font-medium">
                                        {t('pages.outreach.summary.campaign', { defaultValue: 'Campanha' })} #{campaignMutation.data.campaignId}
                                    </span>
                                    <span className="ml-2 text-slate-500">
                                        {t('pages.outreach.summary.segment', { defaultValue: 'Segmento' })}: {getSegmentLabel(campaignMutation.data.segment, t)}
                                    </span>
                                </div>

                                <div className="grid grid-cols-2 gap-3 lg:grid-cols-4" data-testid="outreach-indicators">
                                    {indicators.map((indicator) => (
                                        <Card key={indicator.testId} className="space-y-1 p-4" data-testid={indicator.testId} data-value={indicator.value}>
                                            <p className="text-xs font-medium uppercase tracking-wide text-slate-500">{indicator.label}</p>
                                            <p className="text-2xl font-semibold text-slate-900">{indicator.value}</p>
                                        </Card>
                                    ))}
                                </div>

                                <DataTable
                                    columns={columns}
                                    rows={campaignMutation.data.leads}
                                    rowKey={(row, index) => `${row.leadId}-${index}`}
                                    emptyTitle={t('pages.outreach.empty.noLeadsTitle', { defaultValue: 'Nenhum lead elegivel no segmento selecionado' })}
                                    emptyDescription={t('pages.outreach.empty.noLeadsDescription', { defaultValue: 'Ajuste o segmento ou aumente o limite para tentar novamente.' })}
                                />
                            </div>
                        )
                    ) : (
                        <div data-testid="outreach-results-empty">
                            <EmptyState
                                title={t('pages.outreach.empty.title', { defaultValue: 'Nenhuma campanha executada' })}
                                description={t('pages.outreach.empty.description', { defaultValue: 'Inicie uma campanha para visualizar status por lead e indicadores agregados.' })}
                            />
                        </div>
                    )}
                </div>
            </div>
        </section>
    );
}
