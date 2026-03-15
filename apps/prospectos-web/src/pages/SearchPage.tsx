import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { z } from 'zod';

import Button from '../components/ui/Button';
import Card from '../components/ui/Card';
import type { DataTableColumn } from '../components/ui/DataTable';
import DataTable from '../components/ui/DataTable';
import EmptyState from '../components/ui/EmptyState';
import ErrorState from '../components/ui/ErrorState';
import LoadingState from '../components/ui/LoadingState';
import PageHeader from '../components/ui/PageHeader';
import Select from '../components/ui/Select';
import TextArea from '../components/ui/TextArea';
import { listIcps } from '../services/icpService';
import { acceptLead, searchLeads } from '../services/leadService';
import type { AcceptLeadResponse, LeadResult } from '../types/leadContracts';

const searchFormSchema = z.object({
    query: z.string().min(1),
    limit: z.coerce.number().int().min(1).max(100),
    icpId: z.preprocess((value) => (value === '' ? null : String(value)), z.string().regex(/^-?\d+$/).nullable()),
});

type SearchFormInput = z.input<typeof searchFormSchema>;
type SearchFormValues = z.output<typeof searchFormSchema>;

export default function SearchPage() {
    const { t } = useTranslation();
    const queryClient = useQueryClient();
    const [acceptingLeadKey, setAcceptingLeadKey] = useState<string | null>(null);
    const [acceptedCompaniesByLeadKey, setAcceptedCompaniesByLeadKey] = useState<Record<string, AcceptLeadResponse['company']>>({});
    const [acceptFeedback, setAcceptFeedback] = useState<AcceptLeadResponse['company'] | null>(null);

    const icpsQuery = useQuery({ queryKey: ['icps'], queryFn: listIcps });
    const searchMutation = useMutation({
        mutationFn: searchLeads,
        onSuccess: () => {
            setAcceptedCompaniesByLeadKey({});
            setAcceptFeedback(null);
        },
    });
    const acceptMutation = useMutation({
        mutationFn: acceptLead,
        onSuccess: async (response, variables) => {
            setAcceptedCompaniesByLeadKey((current) => ({
                ...current,
                [variables.leadKey]: response.company,
            }));
            setAcceptFeedback(response.company);
            await queryClient.invalidateQueries({ queryKey: ['companies'] });
        },
    });

    const form = useForm<SearchFormInput, unknown, SearchFormValues>({
        resolver: zodResolver(searchFormSchema),
        defaultValues: {
            query: '',
            limit: 20,
            icpId: null,
        },
    });

    const handleAcceptLead = async (lead: LeadResult) => {
        setAcceptingLeadKey(lead.leadKey);
        setAcceptFeedback(null);

        try {
            await acceptMutation.mutateAsync({
                leadKey: lead.leadKey,
                candidate: lead.candidate,
                score: lead.score,
                source: lead.source,
            });
        } finally {
            setAcceptingLeadKey(null);
        }
    };

    const columns: DataTableColumn<LeadResult>[] = [
        {
            key: 'candidate',
            header: t('pages.companies.table.company'),
            render: (row) => row.candidate.name,
        },
        {
            key: 'industry',
            header: t('pages.companies.table.industry'),
            render: (row) => row.candidate.industry ?? '-',
        },
        {
            key: 'score',
            header: t('pages.companies.table.score'),
            render: (row) => `${row.score.value}/100`,
        },
        {
            key: 'actions',
            header: t('common.actions'),
            render: (row) => {
                const acceptedCompany = acceptedCompaniesByLeadKey[row.leadKey];

                if (acceptedCompany) {
                    return (
                        <Link
                            className="text-sm font-medium text-blue-700 hover:text-blue-800 hover:underline"
                            data-testid={`search-result-view-company-${acceptedCompany.id}`}
                            to={`/companies/${acceptedCompany.id}`}
                        >
                            {t('pages.search.actions.viewCompany')}
                        </Link>
                    );
                }

                return (
                    <Button
                        variant="secondary"
                        loading={acceptMutation.isPending && acceptingLeadKey === row.leadKey}
                        disabled={acceptMutation.isPending}
                        data-testid={`accept-lead-${row.leadKey}`}
                        onClick={() => void handleAcceptLead(row)}
                    >
                        {t('pages.search.actions.accept')}
                    </Button>
                );
            },
        },
    ];

    const onSubmit = form.handleSubmit(async (values) => {
        setAcceptedCompaniesByLeadKey({});
        setAcceptFeedback(null);
        acceptMutation.reset();

        await searchMutation.mutateAsync({
            query: values.query,
            limit: values.limit,
            sources: ['in-memory'],
            icpId: values.icpId,
        });
    });

    return (
        <section className="space-y-4" data-testid="search-page">
            <PageHeader title={t('pages.search.title')} description={t('pages.search.description')} />

            <div className="grid grid-cols-1 gap-4 lg:grid-cols-[1fr,1.2fr]">
                <Card>
                    <form className="space-y-4" onSubmit={onSubmit} data-testid="search-form">
                        <TextArea
                            id="search-query"
                            label={t('pages.search.fields.query')}
                            placeholder={t('pages.search.placeholders.query')}
                            error={form.formState.errors.query?.message}
                            {...form.register('query')}
                        />
                        {icpsQuery.isLoading ? (
                            <LoadingState label={t('common.loading')} />
                        ) : icpsQuery.isError ? (
                            <ErrorState message={t('pages.search.errors.loadIcps')} onRetry={() => void icpsQuery.refetch()} />
                        ) : (
                            <Select id="search-icp" label={t('pages.search.fields.icp')} error={form.formState.errors.icpId?.message} {...form.register('icpId')}>
                                <option value="">{t('pages.search.placeholders.selectIcp')}</option>
                                {(icpsQuery.data ?? []).map((icp) => (
                                    <option key={icp.id} value={icp.id}>
                                        {icp.name}
                                    </option>
                                ))}
                            </Select>
                        )}
                        <Select id="search-limit" label={t('pages.search.fields.limit')} error={form.formState.errors.limit?.message} {...form.register('limit')}>
                            <option value="10">10</option>
                            <option value="20">20</option>
                            <option value="50">50</option>
                            <option value="100">100</option>
                        </Select>
                        <div className="flex justify-end">
                            <Button type="submit" loading={searchMutation.isPending}>{t('common.searchProspects')}</Button>
                        </div>
                    </form>
                </Card>

                <div className="space-y-4">
                    {acceptFeedback ? (
                        <div className="rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700" data-testid="search-accept-feedback">
                            <div>{t('pages.search.feedback.acceptSuccess')}</div>
                            <Link className="mt-2 inline-flex font-medium text-emerald-800 underline" data-testid="search-view-accepted-company" to={`/companies/${acceptFeedback.id}`}>
                                {t('pages.search.actions.viewCompany')}
                            </Link>
                        </div>
                    ) : null}

                    {acceptMutation.isError ? (
                        <ErrorState message={t('pages.search.errors.accept')} />
                    ) : null}

                    {searchMutation.isPending ? (
                        <LoadingState />
                    ) : searchMutation.isError ? (
                        <ErrorState message={t('pages.search.errors.execute')} onRetry={() => void onSubmit()} />
                    ) : searchMutation.data ? (
                        <div data-testid="search-results-table">
                            <DataTable
                                columns={columns}
                                rows={searchMutation.data.leads}
                                rowKey={(row) => row.leadKey}
                                emptyTitle={t('pages.search.empty.title')}
                                emptyDescription={t('pages.search.empty.description')}
                            />
                        </div>
                    ) : (
                        <div data-testid="search-results-empty">
                            <EmptyState title={t('pages.search.empty.title')} description={t('pages.search.empty.description')} />
                        </div>
                    )}
                </div>
            </div>
        </section>
    );
}
