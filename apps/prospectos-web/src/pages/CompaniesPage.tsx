import { useEffect, useMemo, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';

import CompanyFilters, { type CompanyFiltersState } from '../components/features/CompanyFilters';
import CompanyQuickView from '../components/features/CompanyQuickView';
import FilterChips, { type ActiveFilterChip, type CompanyFilterField } from '../components/features/FilterChips';
import Badge from '../components/ui/Badge';
import Button from '../components/ui/Button';
import type { DataTableColumn } from '../components/ui/DataTable';
import DataTable from '../components/ui/DataTable';
import ErrorState from '../components/ui/ErrorState';
import LoadingState from '../components/ui/LoadingState';
import PageHeader from '../components/ui/PageHeader';
import Select from '../components/ui/Select';
import { listCompanies, type CompanyListParams, type CompanyListResponse } from '../services/companyService';
import type { Company } from '../types/companyContracts';

const EMPTY_FILTERS: CompanyFiltersState = {
    query: '',
    industry: '',
    location: '',
    minScore: '',
    maxScore: '',
    hasContact: '',
};

const DEFAULT_PAGE_SIZE = 10;
const PAGE_SIZE_OPTIONS = [5, 10, 20, 50] as const;

function normalizeTextFilter(value: string): string | undefined {
    const normalized = value.trim();
    return normalized ? normalized : undefined;
}

function parseScoreFilter(value: string): number | undefined {
    const normalized = value.trim();
    if (!normalized) {
        return undefined;
    }

    const parsed = Number(normalized);
    if (!Number.isFinite(parsed)) {
        return undefined;
    }

    return Math.max(0, Math.min(100, parsed));
}

function parseHasContactFilter(value: CompanyFiltersState['hasContact']): boolean | undefined {
    if (value === 'true') {
        return true;
    }

    if (value === 'false') {
        return false;
    }

    return undefined;
}

function buildQueryParams(filters: CompanyFiltersState, page: number, size: number): CompanyListParams {
    return {
        query: normalizeTextFilter(filters.query),
        industry: normalizeTextFilter(filters.industry),
        location: normalizeTextFilter(filters.location),
        minScore: parseScoreFilter(filters.minScore),
        maxScore: parseScoreFilter(filters.maxScore),
        hasContact: parseHasContactFilter(filters.hasContact),
        page,
        size,
    };
}

function emptyCompanyPage(page: number, size: number): CompanyListResponse {
    return {
        items: [],
        page,
        size,
        totalItems: 0,
        totalPages: 1,
    };
}

function clampToNonNegative(value: number): number {
    return Number.isFinite(value) && value >= 0 ? value : 0;
}

function resolvePageSize(value: number, fallback: number): number {
    return Number.isFinite(value) && value > 0 ? value : fallback;
}

export default function CompaniesPage() {
    const { t } = useTranslation();
    const [filters, setFilters] = useState<CompanyFiltersState>(EMPTY_FILTERS);
    const [pageSize, setPageSize] = useState<number>(DEFAULT_PAGE_SIZE);
    const [currentPage, setCurrentPage] = useState<number>(0);
    const [quickViewCompany, setQuickViewCompany] = useState<Company | null>(null);

    const queryParams = useMemo(() => buildQueryParams(filters, currentPage, pageSize), [currentPage, filters, pageSize]);

    const companiesQuery = useQuery({
        queryKey: ['companies', queryParams],
        queryFn: () => listCompanies(queryParams),
    });

    const companiesPage = companiesQuery.data ?? emptyCompanyPage(currentPage, pageSize);
    const tableRows = companiesPage.items;
    const totalPages = Math.max(1, companiesPage.totalPages);
    const backendPage = clampToNonNegative(companiesPage.page);
    const backendPageSize = resolvePageSize(companiesPage.size, pageSize);

    useEffect(() => {
        const maxPageIndex = Math.max(0, totalPages - 1);
        const normalizedPage = Math.min(backendPage, maxPageIndex);

        if (currentPage !== normalizedPage) {
            setCurrentPage(normalizedPage);
        }
    }, [backendPage, currentPage, totalPages]);

    const activeFilterChips = useMemo<ActiveFilterChip[]>(() => {
        const chips: ActiveFilterChip[] = [];

        const normalizedQuery = filters.query.trim();
        if (normalizedQuery) {
            chips.push({ key: 'query', label: `Busca: ${normalizedQuery}` });
        }

        const normalizedIndustry = filters.industry.trim();
        if (normalizedIndustry) {
            chips.push({ key: 'industry', label: `Industria: ${normalizedIndustry}` });
        }

        const normalizedLocation = filters.location.trim();
        if (normalizedLocation) {
            chips.push({ key: 'location', label: `Localizacao: ${normalizedLocation}` });
        }

        const normalizedMinScore = filters.minScore.trim();
        if (normalizedMinScore) {
            chips.push({ key: 'minScore', label: `Score >= ${normalizedMinScore}` });
        }

        const normalizedMaxScore = filters.maxScore.trim();
        if (normalizedMaxScore) {
            chips.push({ key: 'maxScore', label: `Score <= ${normalizedMaxScore}` });
        }

        if (filters.hasContact) {
            chips.push({
                key: 'hasContact',
                label: filters.hasContact === 'true' ? 'Com contato' : 'Sem contato',
            });
        }

        return chips;
    }, [filters]);

    const rangeStart = companiesPage.totalItems === 0 || tableRows.length === 0
        ? 0
        : backendPage * backendPageSize + 1;
    const rangeEnd = companiesPage.totalItems === 0 || tableRows.length === 0
        ? 0
        : Math.min(companiesPage.totalItems, rangeStart + tableRows.length - 1);

    const columns: DataTableColumn<Company>[] = [
        {
            key: 'name',
            header: t('pages.companies.table.company'),
            render: (row) => (
                <Link className="font-medium text-blue-700 hover:text-blue-800 hover:underline" data-testid={`company-link-${row.id}`} to={`/companies/${row.id}`}>
                    {row.name}
                </Link>
            ),
        },
        { key: 'industry', header: t('pages.companies.table.industry'), render: (row) => row.industry ?? '-' },
        {
            key: 'primaryContact',
            header: t('pages.companies.table.primaryContact'),
            render: (row) => (
                <div className="space-y-1">
                    <div className="font-medium text-slate-700">{row.primaryContactEmail ?? t('pages.companies.table.noContact')}</div>
                    <div className="text-xs text-slate-500">{t('pages.companies.table.contactCount', { count: row.contactCount })}</div>
                </div>
            ),
        },
        {
            key: 'score',
            header: t('pages.companies.table.score'),
            render: (row) =>
                row.score ? <Badge variant={row.score.value >= 80 ? 'success' : 'warning'}>{row.score.value}/100</Badge> : <Badge variant="neutral">-</Badge>,
        },
        {
            key: 'actions',
            header: 'Acoes',
            render: (row) => (
                <button
                    className="text-sm font-medium text-slate-700 hover:text-slate-900 hover:underline"
                    data-testid={`company-quick-view-trigger-${row.id}`}
                    onClick={() => setQuickViewCompany(row)}
                    type="button"
                >
                    Resumo rapido
                </button>
            ),
        },
    ];

    const handleFiltersChange = (nextFilters: CompanyFiltersState) => {
        setFilters(nextFilters);
        setCurrentPage(0);
    };

    const handleClearFilters = () => {
        setFilters(EMPTY_FILTERS);
        setCurrentPage(0);
    };

    const handleRemoveFilter = (key: CompanyFilterField) => {
        setFilters((currentFilters) => ({
            ...currentFilters,
            [key]: '',
        }));
        setCurrentPage(0);
    };

    const handlePageSizeChange = (value: string) => {
        const nextPageSize = Number(value);

        if (!Number.isFinite(nextPageSize) || nextPageSize <= 0) {
            return;
        }

        setPageSize(nextPageSize);
        setCurrentPage(0);
    };

    if (companiesQuery.isLoading) {
        return <LoadingState />;
    }

    if (companiesQuery.isError) {
        return <ErrorState message={t('pages.companies.errors.load')} onRetry={() => void companiesQuery.refetch()} />;
    }

    return (
        <section className="space-y-4" data-testid="companies-page">
            <PageHeader title={t('pages.companies.title')} description={t('pages.companies.description')} />

            <CompanyFilters
                value={filters}
                onChange={handleFiltersChange}
                onClear={handleClearFilters}
            />

            <FilterChips chips={activeFilterChips} onClear={handleClearFilters} onRemove={handleRemoveFilter} />

            <div className="flex flex-wrap items-end justify-between gap-3" data-testid="companies-pagination-toolbar">
                <div className="text-sm text-slate-600" data-testid="companies-range-indicator">
                    {companiesPage.totalItems === 0
                        ? 'Nenhuma empresa para exibir'
                        : `Mostrando ${rangeStart}-${rangeEnd} de ${companiesPage.totalItems} empresas`}
                </div>

                <div className="w-full md:w-48">
                    <Select id="companies-page-size" label="Itens por pagina" value={String(pageSize)} onChange={(event) => handlePageSizeChange(event.target.value)}>
                        {PAGE_SIZE_OPTIONS.map((option) => (
                            <option key={option} value={option}>
                                {option}
                            </option>
                        ))}
                    </Select>
                </div>
            </div>

            <DataTable
                columns={columns}
                rows={tableRows}
                rowKey={(row) => String(row.id)}
                emptyTitle={t('pages.companies.empty.title')}
                emptyDescription={t('pages.companies.empty.description')}
            />

            {companiesPage.totalItems > 0 ? (
                <div className="flex flex-wrap items-center justify-end gap-2" data-testid="companies-pagination-controls">
                    <span className="mr-2 text-sm text-slate-600" data-testid="companies-page-indicator">
                        Pagina {backendPage + 1} de {totalPages}
                    </span>
                    <Button disabled={backendPage <= 0} onClick={() => setCurrentPage(Math.max(0, backendPage - 1))} variant="secondary">
                        Anterior
                    </Button>
                    <Button disabled={backendPage >= totalPages - 1} onClick={() => setCurrentPage(Math.min(totalPages - 1, backendPage + 1))} variant="secondary">
                        Proxima
                    </Button>
                </div>
            ) : null}

            <CompanyQuickView company={quickViewCompany} open={quickViewCompany !== null} onClose={() => setQuickViewCompany(null)} />
        </section>
    );
}
