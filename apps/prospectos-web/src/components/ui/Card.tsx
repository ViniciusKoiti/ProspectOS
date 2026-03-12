import type { HTMLAttributes, ReactNode } from 'react';

import { cn } from '../../utils/cn';

type CardProps = HTMLAttributes<HTMLDivElement> & {
    children: ReactNode;
};

export default function Card({ children, className, ...props }: CardProps) {
    return (
        <div className={cn('rounded-xl border border-slate-200 bg-white p-6 shadow-sm', className)} {...props}>
            {children}
        </div>
    );
}
