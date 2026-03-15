import { useTranslation } from 'react-i18next';

import type { CompanyContact } from '../../types/companyContracts';
import type { DataTableColumn } from '../ui/DataTable';
import DataTable from '../ui/DataTable';
import EmptyState from '../ui/EmptyState';
import ErrorState from '../ui/ErrorState';
import LoadingState from '../ui/LoadingState';

type CompanyContactsSectionProps = {
    contacts: CompanyContact[] | undefined;
    isLoading: boolean;
    isError: boolean;
    onRetry: () => void;
};

export default function CompanyContactsSection({ contacts, isError, isLoading, onRetry }: CompanyContactsSectionProps) {
    const { t } = useTranslation();

    if (isLoading) {
        return <LoadingState label={t('pages.companyDetail.contacts.loading')} />;
    }

    if (isError) {
        return <ErrorState message={t('pages.companyDetail.contacts.errors.load')} onRetry={onRetry} />;
    }

    const rows = contacts ?? [];
    if (rows.length === 0) {
        return (
            <EmptyState
                title={t('pages.companyDetail.contacts.empty.title')}
                description={t('pages.companyDetail.contacts.empty.description')}
            />
        );
    }

    const columns: DataTableColumn<CompanyContact>[] = [
        {
            key: 'name',
            header: t('pages.companyDetail.contacts.table.name'),
            render: (row) => row.name,
        },
        {
            key: 'position',
            header: t('pages.companyDetail.contacts.table.position'),
            render: (row) => row.position ?? '-',
        },
        {
            key: 'email',
            header: t('pages.companyDetail.contacts.table.email'),
            render: (row) => row.email,
        },
        {
            key: 'phoneNumber',
            header: t('pages.companyDetail.contacts.table.phoneNumber'),
            render: (row) => row.phoneNumber ?? '-',
        },
    ];

    return <DataTable columns={columns} rows={rows} rowKey={(row, index) => `${row.email}-${index}`} />;
}
