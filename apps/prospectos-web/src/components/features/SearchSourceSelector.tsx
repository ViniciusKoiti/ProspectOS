import { cn } from '../../utils/cn';
import FieldLabel from '../ui/FieldLabel';

export type SearchSourceOption = {
    value: string;
    label: string;
    description: string;
};

type SearchSourceSelectorProps = {
    label: string;
    options: SearchSourceOption[];
    selectedSources: string[];
    onChange: (sources: string[]) => void;
    error?: string;
    hint?: string;
    disabled?: boolean;
};

export default function SearchSourceSelector({
    disabled = false,
    error,
    hint,
    label,
    onChange,
    options,
    selectedSources,
}: SearchSourceSelectorProps) {
    const selectedSet = new Set(selectedSources);

    const handleToggle = (source: string) => {
        const isSelected = selectedSet.has(source);
        const nextSources = isSelected ? selectedSources.filter((value) => value !== source) : [...selectedSources, source];
        const orderedSources = options.map((option) => option.value).filter((value) => nextSources.includes(value));

        onChange(orderedSources);
    };

    return (
        <FieldLabel error={error} hint={hint} label={label}>
            <div className="grid gap-2 sm:grid-cols-3">
                {options.map((option) => {
                    const checked = selectedSet.has(option.value);
                    const isLastSelected = checked && selectedSources.length === 1;

                    return (
                        <label
                            key={option.value}
                            className={cn(
                                'flex cursor-pointer items-start gap-3 rounded-lg border px-3 py-2 transition',
                                checked ? 'border-blue-500 bg-blue-50 text-slate-900' : 'border-slate-300 bg-white text-slate-700',
                                disabled ? 'cursor-not-allowed opacity-70' : 'hover:border-blue-400'
                            )}
                        >
                            <input
                                type="checkbox"
                                className="mt-0.5 h-4 w-4 rounded border-slate-300 text-blue-600 focus:ring-blue-500"
                                checked={checked}
                                disabled={disabled || isLastSelected}
                                onChange={() => handleToggle(option.value)}
                            />
                            <span className="space-y-0.5">
                                <span className="block text-sm font-medium">{option.label}</span>
                                <span className="block text-xs text-slate-600">{option.description}</span>
                            </span>
                        </label>
                    );
                })}
            </div>
        </FieldLabel>
    );
}
