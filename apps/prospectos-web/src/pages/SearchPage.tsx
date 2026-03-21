import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useEffect, useMemo, useRef, useState } from 'react';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { z } from 'zod';

import SearchMatchInsights from '../components/features/SearchMatchInsights';
import SearchSourceSelector, { type SearchSourceOption } from '../components/features/SearchSourceSelector';
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
import TextArea from '../components/ui/TextArea';
import { listIcps } from '../services/icpService';
import { acceptLead, openLeadSearchEvents, startLeadSearchAsync } from '../services/leadService';
import type { AcceptLeadResponse, LeadResult, LeadSearchAsyncSnapshot, WebsitePresence } from '../types/leadContracts';
import {
    buildSearchResultsCsv,
    filterLeadsByWebsitePresence,
    getScoreBadgeVariant,
    getWebsitePresenceBadgeVariant,
    getWebsitePresenceLabel,
    isSearchSourceValue,
    mergeWithFallbackError,
    parseApiErrorMessage,
    SEARCH_SOURCE_VALUES,
    WEBSITE_PRESENCE_FILTER_VALUES,
} from './search/searchUtils';

const searchSourceSchema = z.enum(SEARCH_SOURCE_VALUES);
const websitePresenceFilterSchema = z.enum(WEBSITE_PRESENCE_FILTER_VALUES);

const searchFormSchema = z.object({
    query: z.string().min(1),
    limit: z.coerce.number().int().min(1).max(100),
    icpId: z.preprocess((value) => (value === '' ? null : String(value)), z.string().regex(/^-?\d+$/).nullable()),
    sources: z.array(searchSourceSchema).min(1, 'Select at least one source.'),
    websitePresence: websitePresenceFilterSchema.default('all'),
});

type SearchFormInput = z.input<typeof searchFormSchema>;
type SearchFormValues = z.output<typeof searchFormSchema>;

type SearchDisplayState = {
    status: 'PROCESSING' | 'COMPLETED' | 'FAILED';
    message: string | null;
    leads: LeadResult[];
};

function formatWebsitePresenceLabel(websitePresence: WebsitePresence, t: (key: string, options?: { defaultValue?: string }) => string): string {
    return getWebsitePresenceLabel(websitePresence, {
        hasWebsite: t('pages.search.websitePresence.hasWebsite', { defaultValue: 'Com site' }),
        noWebsite: t('pages.search.websitePresence.noWebsite', { defaultValue: 'Sem site' }),
        unknown: t('pages.search.websitePresence.unknown', { defaultValue: 'Desconhecido' }),
    });
}

function toSearchDisplayState(searchSnapshot: LeadSearchAsyncSnapshot | null, startResponse: unknown): SearchDisplayState | null {
    if (searchSnapshot) {
        return {
            status: searchSnapshot.status,
            message: searchSnapshot.message,
            leads: searchSnapshot.leads,
        };
    }

    if (!startResponse || typeof startResponse !== 'object') {
        return null;
    }

    const candidate = startResponse as Partial<{
        status: string;
        message: string | null;
        leads: LeadResult[];
    }>;

    if (candidate.status !== 'PROCESSING' && candidate.status !== 'COMPLETED' && candidate.status !== 'FAILED') {
        return null;
    }

    return {
        status: candidate.status,
        message: candidate.message ?? null,
        leads: Array.isArray(candidate.leads) ? candidate.leads : [],
    };
}

export default function SearchPage() {
    const { t } = useTranslation();
    const queryClient = useQueryClient();
    const streamRef = useRef<EventSource | null>(null);
    const [searchSnapshot, setSearchSnapshot] = useState<LeadSearchAsyncSnapshot | null>(null);
    const [searchStreamError, setSearchStreamError] = useState<string | null>(null);
    const [acceptingLeadKey, setAcceptingLeadKey] = useState<string | null>(null);
    const [acceptedCompaniesByLeadKey, setAcceptedCompaniesByLeadKey] = useState<Record<string, AcceptLeadResponse['company']>>({});
    const [acceptFeedback, setAcceptFeedback] = useState<AcceptLeadResponse['company'] | null>(null);

    useEffect(() => {
        return () => {
            if (streamRef.current) {
                streamRef.current.close();
                streamRef.current = null;
            }
        };
    }, []);

    const closeActiveStream = () => {
        if (!streamRef.current) {
            return;
        }
        streamRef.current.close();
        streamRef.current = null;
    };

    const icpsQuery = useQuery({ queryKey: ['icps'], queryFn: listIcps });
    const startSearchMutation = useMutation({
        mutationFn: startLeadSearchAsync,
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
            sources: ['in-memory'],
            websitePresence: 'all',
        },
    });

    const selectedSources = form.watch('sources');
    const selectedIcpId = form.watch('icpId');
    const selectedWebsitePresence = form.watch('websitePresence') ?? 'all';
    const selectedIcpName = selectedIcpId
        ? (icpsQuery.data ?? []).find((icp) => icp.id === selectedIcpId)?.name ?? null
        : null;

    const sourceOptions: SearchSourceOption[] = [
        {
            value: 'in-memory',
            label: t('pages.search.sources.inMemory', { defaultValue: 'in-memory' }),
            description: t('pages.search.sources.inMemoryDescription', { defaultValue: 'Mock source for safe local testing.' }),
        },
        {
            value: 'vector-company',
            label: t('pages.search.sources.vectorCompany', { defaultValue: 'vector-company' }),
            description: t('pages.search.sources.vectorCompanyDescription', { defaultValue: 'Semantic match against indexed companies.' }),
        },
        {
            value: 'cnpj-ws',
            label: t('pages.search.sources.cnpjWs', { defaultValue: 'cnpj-ws' }),
            description: t('pages.search.sources.cnpjWsDescription', { defaultValue: 'Discovery using CNPJ web data.' }),
        },
    ];

    const searchDisplayState = useMemo(
        () => toSearchDisplayState(searchSnapshot, startSearchMutation.data),
        [searchSnapshot, startSearchMutation.data]
    );

    const filteredLeads = useMemo(
        () => filterLeadsByWebsitePresence(searchDisplayState?.leads ?? [], selectedWebsitePresence),
        [searchDisplayState?.leads, selectedWebsitePresence]
    );

    const isNoWebsiteEmptyState = selectedWebsitePresence === 'NO_WEBSITE' && filteredLeads.length === 0;
    const resultsEmptyTitle = isNoWebsiteEmptyState
        ? t('pages.search.empty.noWebsiteTitle', { defaultValue: 'Nenhum lead sem site encontrado' })
        : t('pages.search.empty.title');
    const resultsEmptyDescription = isNoWebsiteEmptyState
        ? t('pages.search.empty.noWebsiteDescription', { defaultValue: 'Ajuste os filtros ou execute uma nova busca para encontrar empresas sem site.' })
        : t('pages.search.empty.description');

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

    const handleExportCsv = () => {
        if (filteredLeads.length === 0) {
            return;
        }

        const csv = buildSearchResultsCsv(filteredLeads);
        const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
        const blobUrl = URL.createObjectURL(blob);
        const link = document.createElement('a');
        const timestamp = new Date().toISOString().replaceAll(':', '-').replaceAll('.', '-');

        link.href = blobUrl;
        link.download = `search-results-${timestamp}.csv`;
        document.body.append(link);
        link.click();
        link.remove();
        URL.revokeObjectURL(blobUrl);
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
            key: 'websitePresence',
            header: t('pages.search.table.websitePresence', { defaultValue: 'Website presence' }),
            render: (row) => (
                <Badge variant={getWebsitePresenceBadgeVariant(row.candidate.websitePresence)}>
                    {formatWebsitePresenceLabel(row.candidate.websitePresence, t)}
                </Badge>
            ),
        },
        {
            key: 'source',
            header: t('pages.search.table.source', { defaultValue: 'Source' }),
            render: (row) => row.source.sourceName,
        },
        {
            key: 'score',
            header: t('pages.companies.table.score'),
            render: (row) => (
                <div className="flex items-center gap-2">
                    <span>{row.score.value}/100</span>
                    <Badge variant={getScoreBadgeVariant(row.score.category)}>{row.score.category}</Badge>
                </div>
            ),
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
        closeActiveStream();
        setSearchSnapshot(null);
        setSearchStreamError(null);
        setAcceptedCompaniesByLeadKey({});
        setAcceptFeedback(null);
        acceptMutation.reset();

        const startResponse = await startSearchMutation.mutateAsync({
            query: values.query,
            limit: values.limit,
            sources: values.sources,
            icpId: values.icpId,
        });

        const eventStream = openLeadSearchEvents(startResponse.requestId, {
            onSnapshot: (snapshot) => {
                setSearchSnapshot(snapshot);
                if (snapshot.status !== 'PROCESSING') {
                    eventStream.close();
                    if (streamRef.current === eventStream) {
                        streamRef.current = null;
                    }
                }
            },
            onError: () => {
                setSearchStreamError(
                    t('pages.search.errors.sseConnection', { defaultValue: 'Falha na conexao de atualizacao em tempo real.' })
                );
            },
        });
        streamRef.current = eventStream;
    });

    const searchErrorMessage = startSearchMutation.isError
        ? mergeWithFallbackError(t('pages.search.errors.execute'), parseApiErrorMessage(startSearchMutation.error))
        : null;
    const acceptErrorMessage = acceptMutation.isError
        ? mergeWithFallbackError(t('pages.search.errors.accept'), parseApiErrorMessage(acceptMutation.error))
        : null;
    const processingMessage = searchSnapshot?.progress.totalSources && searchSnapshot.progress.totalSources > 0
        ? `${searchDisplayState?.message ?? t('common.loading')} (${searchSnapshot.progress.doneSources}/${searchSnapshot.progress.totalSources})`
        : searchDisplayState?.message ?? t('common.loading');

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
                        <SearchSourceSelector
                            label={t('pages.search.fields.sources', { defaultValue: 'Sources' })}
                            hint={t('pages.search.fields.sourcesHint', { defaultValue: 'Select one or more lead sources.' })}
                            error={form.formState.errors.sources?.message}
                            options={sourceOptions}
                            selectedSources={selectedSources}
                            onChange={(sources) => {
                                const validSources = sources.filter(isSearchSourceValue);
                                form.setValue('sources', validSources, { shouldDirty: true, shouldValidate: true });
                            }}
                        />
                        <Select id="search-limit" label={t('pages.search.fields.limit')} error={form.formState.errors.limit?.message} {...form.register('limit')}>
                            <option value="10">10</option>
                            <option value="20">20</option>
                            <option value="50">50</option>
                            <option value="100">100</option>
                        </Select>
                        <div className="flex justify-end">
                            <Button type="submit" loading={startSearchMutation.isPending}>{t('common.searchProspects')}</Button>
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

                    {acceptErrorMessage ? <ErrorState message={acceptErrorMessage} /> : null}

                    {startSearchMutation.isPending ? (
                        <LoadingState label={t('pages.search.loading.starting', { defaultValue: 'Iniciando busca...' })} />
                    ) : searchErrorMessage ? (
                        <ErrorState message={searchErrorMessage} onRetry={() => void onSubmit()} />
                    ) : searchStreamError && searchDisplayState?.status === 'PROCESSING' ? (
                        <ErrorState message={searchStreamError} onRetry={() => void onSubmit()} />
                    ) : searchDisplayState?.status === 'PROCESSING' ? (
                        <LoadingState label={processingMessage} />
                    ) : searchDisplayState?.status === 'FAILED' ? (
                        <ErrorState
                            message={searchDisplayState.message ?? t('pages.search.errors.execute')}
                            onRetry={() => void onSubmit()}
                        />
                    ) : searchDisplayState?.status === 'COMPLETED' ? (
                        <div className="space-y-4" data-testid="search-results-table">
                            <SearchMatchInsights leads={filteredLeads} selectedIcpName={selectedIcpName} />
                            <div className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
                                <Select
                                    id="search-website-presence"
                                    label={t('pages.search.fields.websitePresence', { defaultValue: 'Website presence' })}
                                    {...form.register('websitePresence')}
                                >
                                    <option value="all">{t('pages.search.websitePresence.all', { defaultValue: 'Todos' })}</option>
                                    <option value="HAS_WEBSITE">{t('pages.search.websitePresence.hasWebsite', { defaultValue: 'Com site' })}</option>
                                    <option value="NO_WEBSITE">{t('pages.search.websitePresence.noWebsite', { defaultValue: 'Sem site' })}</option>
                                </Select>
                                <Button
                                    variant="secondary"
                                    data-testid="search-export-csv"
                                    disabled={filteredLeads.length === 0}
                                    onClick={handleExportCsv}
                                >
                                    {t('pages.search.actions.exportCsv', { defaultValue: 'Export CSV' })}
                                </Button>
                            </div>
                            <DataTable
                                columns={columns}
                                rows={filteredLeads}
                                rowKey={(row) => row.leadKey}
                                emptyTitle={resultsEmptyTitle}
                                emptyDescription={resultsEmptyDescription}
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
