import type { ReactNode } from 'react';

type FieldLabelProps = {
    htmlFor?: string;
    label: string;
    hint?: string;
    error?: string;
    children: ReactNode;
};

export default function FieldLabel({ children, error, hint, htmlFor, label }: FieldLabelProps) {
    return (
        <label className="block space-y-1.5 text-sm" htmlFor={htmlFor}>
            <span className="font-medium text-slate-700">{label}</span>
            {children}
            {error ? <span className="block text-xs text-red-600">{error}</span> : null}
            {!error && hint ? <span className="block text-xs text-slate-500">{hint}</span> : null}
        </label>
    );
}
