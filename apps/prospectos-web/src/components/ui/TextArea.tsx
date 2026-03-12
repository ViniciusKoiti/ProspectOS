import type { TextareaHTMLAttributes } from 'react';

import { cn } from '../../utils/cn';
import FieldLabel from './FieldLabel';

type TextAreaProps = TextareaHTMLAttributes<HTMLTextAreaElement> & {
    label: string;
    error?: string;
    hint?: string;
};

export default function TextArea({ className, error, hint, id, label, rows = 4, ...props }: TextAreaProps) {
    return (
        <FieldLabel error={error} hint={hint} htmlFor={id} label={label}>
            <textarea
                id={id}
                rows={rows}
                className={cn(
                    'w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900',
                    'focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/30',
                    error ? 'border-red-400 focus:border-red-500 focus:ring-red-500/30' : '',
                    className
                )}
                {...props}
            />
        </FieldLabel>
    );
}
