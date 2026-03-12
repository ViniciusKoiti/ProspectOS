import type { ReactNode } from 'react';

import { cn } from '../../utils/cn';

type BadgeVariant = 'neutral' | 'success' | 'warning';

type BadgeProps = {
    children: ReactNode;
    variant?: BadgeVariant;
};

const variantClass: Record<BadgeVariant, string> = {
    neutral: 'bg-slate-100 text-slate-700',
    success: 'bg-emerald-100 text-emerald-700',
    warning: 'bg-amber-100 text-amber-700',
};

export default function Badge({ children, variant = 'neutral' }: BadgeProps) {
    return <span className={cn('inline-flex rounded-full px-2.5 py-1 text-xs font-medium', variantClass[variant])}>{children}</span>;
}
