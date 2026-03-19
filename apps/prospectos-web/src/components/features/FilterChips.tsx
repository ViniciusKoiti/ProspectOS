import Button from '../ui/Button';
import type { CompanyFiltersState } from './CompanyFilters';

export type CompanyFilterField = keyof CompanyFiltersState;

export type ActiveFilterChip = {
    key: CompanyFilterField;
    label: string;
};

type FilterChipsProps = {
    chips: ActiveFilterChip[];
    onClear: () => void;
    onRemove: (key: CompanyFilterField) => void;
};

export default function FilterChips({ chips, onClear, onRemove }: FilterChipsProps) {
    if (chips.length === 0) {
        return null;
    }

    return (
        <div className="flex flex-wrap items-center gap-2" data-testid="company-filter-chips">
            {chips.map((chip) => (
                <button
                    key={`${chip.key}-${chip.label}`}
                    className="inline-flex items-center gap-2 rounded-full border border-slate-200 bg-slate-100 px-3 py-1 text-xs font-medium text-slate-700 transition hover:bg-slate-200"
                    onClick={() => onRemove(chip.key)}
                    type="button"
                >
                    <span>{chip.label}</span>
                    <span aria-hidden="true" className="text-slate-500">x</span>
                </button>
            ))}

            <Button className="px-3 py-1.5 text-xs" onClick={onClear} variant="ghost">
                Limpar filtros
            </Button>
        </div>
    );
}
