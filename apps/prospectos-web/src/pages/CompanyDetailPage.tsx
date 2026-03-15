import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { Link, useParams } from 'react-router-dom';

import CompanyContactsSection from '../components/features/CompanyContactsSection';
import Badge from '../components/ui/Badge';
import Card from '../components/ui/Card';
import ErrorState from '../components/ui/ErrorState';
import LoadingState from '../components/ui/LoadingState';
import PageHeader from '../components/ui/PageHeader';
import { getCompany, getCompanyContacts } from '../services/companyService';

const COMPANY_ID_PATTERN = /^-?\d+$/;

export default function CompanyDetailPage() {
    const { t } = useTranslation();
    const { id } = useParams();
    const companyId = id ?? '';
    const hasValidCompanyId = COMPANY_ID_PATTERN.test(companyId);

    const companyQuery = useQuery({
        queryKey: ['company', companyId],
        queryFn: () => getCompany(companyId),
        enabled: hasValidCompanyId,
    });

    const companyContactsQuery = useQuery({
        queryKey: ['company-contacts', companyId],
        queryFn: () => getCompanyContacts(companyId),
        enabled: hasValidCompanyId,
    });

    if (!hasValidCompanyId) {
        return <ErrorState message={t('pages.companyDetail.errors.invalidId')} />;
    }

    if (companyQuery.isLoading) {
        return <LoadingState />;
    }

    if (companyQuery.isError || !companyQuery.data) {
        return <ErrorState message={t('pages.companyDetail.errors.load')} onRetry={() => void companyQuery.refetch()} />;
    }

    const company = companyQuery.data;

    return (
        <section className="space-y-4" data-testid="company-detail-page">
            <PageHeader title={t('pages.companyDetail.title')} description={t('pages.companyDetail.selectedId', { id: company.id })} />

            <Card className="space-y-4">
                <div className="flex items-center justify-between">
                    <h2 className="text-lg font-semibold text-slate-900">{company.name}</h2>
                    <Badge variant={company.score && company.score.value >= 80 ? 'success' : 'neutral'}>
                        {company.score ? `${company.score.value}/100` : t('pages.companyDetail.scoreUnavailable')}
                    </Badge>
                </div>
                <p className="text-sm text-slate-600">{company.description ?? t('pages.companyDetail.description')}</p>
                <p className="text-sm text-slate-700">{company.industry ?? '-'} · {company.location ?? '-'}</p>
                <p className="text-sm text-slate-700">
                    {t('pages.companyDetail.contacts.primary')}: {company.primaryContactEmail ?? t('pages.companies.table.noContact')}
                </p>
                <p className="text-sm text-slate-700">{t('pages.companyDetail.contacts.count', { count: company.contactCount })}</p>
                <Link className="text-sm font-medium text-blue-700 hover:text-blue-800 hover:underline" to="/companies">
                    {t('common.backToCompanies')}
                </Link>
            </Card>

            <Card className="space-y-4">
                <h3 className="text-base font-semibold text-slate-900">{t('pages.companyDetail.contacts.title')}</h3>
                <CompanyContactsSection
                    contacts={companyContactsQuery.data}
                    isLoading={companyContactsQuery.isLoading}
                    isError={companyContactsQuery.isError}
                    onRetry={() => void companyContactsQuery.refetch()}
                />
            </Card>
        </section>
    );
}

