import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';

import StatCard from '../components/features/StatCard';
import Button from '../components/ui/Button';
import ErrorState from '../components/ui/ErrorState';
import LoadingState from '../components/ui/LoadingState';
import PageHeader from '../components/ui/PageHeader';
import { listCompanies } from '../services/companyService';
import { listIcps } from '../services/icpService';

type StatItem = {
    label: string;
    value: string;
    trend: string;
};

export default function DashboardPage() {
    const { t } = useTranslation();
    const companiesQuery = useQuery({ queryKey: ['companies'], queryFn: listCompanies });
    const icpsQuery = useQuery({ queryKey: ['icps'], queryFn: listIcps });

    const refreshDashboard = () => {
        void companiesQuery.refetch();
        void icpsQuery.refetch();
    };

    if (companiesQuery.isLoading || icpsQuery.isLoading) {
        return <LoadingState />;
    }

    if (companiesQuery.isError || icpsQuery.isError) {
        return <ErrorState message={t('pages.dashboard.errors.load')} onRetry={refreshDashboard} />;
    }

    const companyCount = companiesQuery.data?.length ?? 0;
    const icpCount = icpsQuery.data?.length ?? 0;
    const stats: StatItem[] = [
        { label: t('pages.dashboard.stats.companies'), value: String(companyCount), trend: t('pages.dashboard.stats.trend') },
        { label: t('pages.dashboard.stats.activeIcps'), value: String(icpCount), trend: t('pages.dashboard.stats.trend') },
        { label: t('pages.dashboard.stats.searchesToday'), value: '0', trend: t('pages.dashboard.stats.trend') },
    ];

    return (
        <section className="space-y-4">
            <PageHeader
                title={t('pages.dashboard.title')}
                description={t('pages.dashboard.description')}
                action={<Button variant="secondary" onClick={refreshDashboard}>{t('common.refresh')}</Button>}
            />

            <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
                {stats.map((item) => (
                    <StatCard key={item.label} label={item.label} trend={item.trend} value={item.value} />
                ))}
            </div>
        </section>
    );
}
