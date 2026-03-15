import { Link } from 'react-router-dom';

import type { Company } from '../../types/companyContracts';
import Badge from '../ui/Badge';
import Button from '../ui/Button';
import Modal from '../ui/Modal';

type CompanyQuickViewProps = {
    company: Company | null;
    open: boolean;
    onClose: () => void;
};

function fallbackValue(value: string | number | null | undefined, fallback: string): string {
    if (value === null || value === undefined) {
        return fallback;
    }

    if (typeof value === 'string') {
        const trimmedValue = value.trim();
        return trimmedValue.length > 0 ? trimmedValue : fallback;
    }

    return String(value);
}

export default function CompanyQuickView({ company, onClose, open }: CompanyQuickViewProps) {
    const scoreVariant = company?.score ? (company.score.value >= 80 ? 'success' : 'warning') : 'neutral';
    const scoreLabel = company?.score ? `${company.score.value}/100` : 'Sem score';

    return (
        <Modal
            open={open}
            title={company ? `Resumo rapido: ${company.name}` : 'Resumo rapido'}
            description="Visualize dados principais sem sair da listagem"
            onClose={onClose}
            footer={
                <div className="flex items-center justify-end gap-3">
                    <Button onClick={onClose} variant="secondary">
                        Fechar
                    </Button>
                    {company ? (
                        <Link
                            className="inline-flex items-center justify-center rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white transition hover:bg-blue-700"
                            to={`/companies/${company.id}`}
                        >
                            Ver detalhes
                        </Link>
                    ) : null}
                </div>
            }
        >
            {company ? (
                <div className="space-y-4" data-testid={`company-quick-view-${company.id}`}>
                    <div className="flex flex-wrap items-center justify-between gap-2">
                        <h3 className="text-lg font-semibold text-slate-900">{company.name}</h3>
                        <Badge variant={scoreVariant}>{scoreLabel}</Badge>
                    </div>

                    <p className="text-sm text-slate-600">{fallbackValue(company.description, 'Sem descricao cadastrada')}</p>

                    <dl className="grid gap-3 text-sm text-slate-700 sm:grid-cols-2">
                        <div>
                            <dt className="font-medium text-slate-900">Industria</dt>
                            <dd>{fallbackValue(company.industry, '-')}</dd>
                        </div>
                        <div>
                            <dt className="font-medium text-slate-900">Localizacao</dt>
                            <dd>{fallbackValue(company.location, '-')}</dd>
                        </div>
                        <div>
                            <dt className="font-medium text-slate-900">Contato principal</dt>
                            <dd>{fallbackValue(company.primaryContactEmail, 'Sem contato')}</dd>
                        </div>
                        <div>
                            <dt className="font-medium text-slate-900">Total de contatos</dt>
                            <dd>{company.contactCount}</dd>
                        </div>
                        <div>
                            <dt className="font-medium text-slate-900">Site</dt>
                            <dd>{fallbackValue(company.website, '-')}</dd>
                        </div>
                        <div>
                            <dt className="font-medium text-slate-900">Colaboradores</dt>
                            <dd>{fallbackValue(company.employeeCount, '-')}</dd>
                        </div>
                    </dl>
                </div>
            ) : null}
        </Modal>
    );
}
