import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';

import Button from '../components/ui/Button';
import type { DataTableColumn } from '../components/ui/DataTable';
import DataTable from '../components/ui/DataTable';
import ErrorState from '../components/ui/ErrorState';
import LoadingState from '../components/ui/LoadingState';
import PageHeader from '../components/ui/PageHeader';
import { listIcps } from '../services/icpService';
import type { Icp } from '../types/contracts';

export default function IcpsPage() {
    const { t } = useTranslation();
    const icpsQuery = useQuery({ queryKey: ['icps'], queryFn: listIcps });

    const columns: DataTableColumn<Icp>[] = [
        { key: 'name', header: t('pages.icps.table.name'), render: (row) => row.name },
        { key: 'focus', header: t('pages.icps.table.focus'), render: (row) => row.targetRoles.join(', ') || '-' },
        { key: 'regions', header: t('pages.icps.table.regions'), render: (row) => row.regions.join(', ') || '-' },
    ];

    if (icpsQuery.isLoading) {
        return <LoadingState />;
    }

    if (icpsQuery.isError) {
        return <ErrorState message="Failed to load ICPs." onRetry={() => void icpsQuery.refetch()} />;
    }

    return (
        <section className="space-y-4">
            <PageHeader
                title={t('pages.icps.title')}
                description={t('pages.icps.description')}
                action={<Button>{t('common.newIcp')}</Button>}
            />

            <DataTable
                columns={columns}
                rows={icpsQuery.data ?? []}
                rowKey={(row) => String(row.id)}
                emptyTitle={t('pages.icps.empty.title')}
                emptyDescription={t('pages.icps.empty.description')}
            />
        </section>
    );
}
