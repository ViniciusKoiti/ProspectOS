import { useMemo, useState } from 'react';

import Button from '../ui/Button';
import Card from '../ui/Card';
import Input from '../ui/Input';
import Select from '../ui/Select';

export type CompanyHasContactFilter = '' | 'true' | 'false';

export type CompanyFiltersState = {
    query: string;
    industry: string;
    location: string;
    minScore: string;
    maxScore: string;
    hasContact: CompanyHasContactFilter;
};

type CompanyFiltersProps = {
    value: CompanyFiltersState;
    onChange: (nextValue: CompanyFiltersState) => void;
    onClear: () => void;
};

export default function CompanyFilters({ onChange, onClear, value }: CompanyFiltersProps) {
    const [showAdvanced, setShowAdvanced] = useState(true);
    const hasActiveFilters = useMemo(
        () => Boolean(
            value.query.trim()
                || value.industry.trim()
                || value.location.trim()
                || value.minScore.trim()
                || value.maxScore.trim()
                || value.hasContact
        ),
        [value.hasContact, value.industry, value.location, value.maxScore, value.minScore, value.query]
    );

    const setFilter = <K extends keyof CompanyFiltersState>(key: K, filterValue: CompanyFiltersState[K]) => {
        onChange({
            ...value,
            [key]: filterValue,
        });
    };

    return (
        <Card className="space-y-4" data-testid="company-filters">
            <div className="flex flex-wrap items-center justify-between gap-3">
                <h2 className="text-base font-semibold text-slate-900">Filtros</h2>
                <div className="flex flex-wrap items-center gap-2">
                    <Button
                        className="px-3 py-1.5 text-xs"
                        onClick={() => setShowAdvanced((current) => !current)}
                        variant="ghost"
                    >
                        {showAdvanced ? 'Ocultar avancados' : 'Mostrar avancados'}
                    </Button>
                    <Button className="px-3 py-1.5 text-xs" disabled={!hasActiveFilters} onClick={onClear} variant="secondary">
                        Limpar filtros
                    </Button>
                </div>
            </div>

            <div className="grid gap-4 md:grid-cols-2">
                <Input
                    id="company-filter-query"
                    label="Busca"
                    placeholder="Ex.: alpha software b2b"
                    value={value.query}
                    onChange={(event) => setFilter('query', event.target.value)}
                />
                <Input
                    id="company-filter-industry"
                    label="Industria"
                    placeholder="Ex.: Software"
                    value={value.industry}
                    onChange={(event) => setFilter('industry', event.target.value)}
                />
            </div>

            {showAdvanced ? (
                <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
                    <Input
                        id="company-filter-location"
                        label="Localizacao"
                        placeholder="Ex.: Sao Paulo"
                        value={value.location}
                        onChange={(event) => setFilter('location', event.target.value)}
                    />
                    <Input
                        id="company-filter-min-score"
                        type="number"
                        min={0}
                        max={100}
                        step={1}
                        label="Score minimo"
                        placeholder="0 a 100"
                        value={value.minScore}
                        onChange={(event) => setFilter('minScore', event.target.value)}
                    />
                    <Input
                        id="company-filter-max-score"
                        type="number"
                        min={0}
                        max={100}
                        step={1}
                        label="Score maximo"
                        placeholder="0 a 100"
                        value={value.maxScore}
                        onChange={(event) => setFilter('maxScore', event.target.value)}
                    />
                    <Select
                        id="company-filter-has-contact"
                        label="Com contato"
                        value={value.hasContact}
                        onChange={(event) => setFilter('hasContact', event.target.value as CompanyHasContactFilter)}
                    >
                        <option value="">Todos</option>
                        <option value="true">Somente com contato</option>
                        <option value="false">Sem contato</option>
                    </Select>
                </div>
            ) : null}
        </Card>
    );
}
