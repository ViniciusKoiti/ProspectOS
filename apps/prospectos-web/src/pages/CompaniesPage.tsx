import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';

import Badge from '../components/ui/Badge';
import type { DataTableColumn } from '../components/ui/DataTable';
import DataTable from '../components/ui/DataTable';
import ErrorState from '../components/ui/ErrorState';
import LoadingState from '../components/ui/LoadingState';
import PageHeader from '../components/ui/PageHeader';
import { listCompanies } from '../services/companyService';
import type { Company } from '../types/companyContracts';

export default function CompaniesPage() {
    const { t } = useTranslation();
    const companiesQuery = useQuery({ queryKey: ['companies'], queryFn: listCompanies });

    const columns: DataTableColumn<Company>[] = [
        {
            key: 'name',
            header: t('pages.companies.table.company'),
            render: (row) => (
                <Link className="font-medium text-blue-700 hover:text-blue-800 hover:underline" to={`/companies/${row.id}`}>
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
    ];

    if (companiesQuery.isLoading) {
        return <LoadingState />;
    }

    if (companiesQuery.isError) {
        return <ErrorState message={t('pages.companies.errors.load')} onRetry={() => void companiesQuery.refetch()} />;
    }

    return (
        <section className="space-y-4">
            <PageHeader title={t('pages.companies.title')} description={t('pages.companies.description')} />
            <DataTable
                columns={columns}
                rows={companiesQuery.data ?? []}
                rowKey={(row) => String(row.id)}
                emptyTitle={t('pages.companies.empty.title')}
                emptyDescription={t('pages.companies.empty.description')}
            />
        </section>
    );
}
