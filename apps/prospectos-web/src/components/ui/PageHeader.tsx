import type { ReactNode } from 'react';

type PageHeaderProps = {
    title: string;
    description?: string;
    action?: ReactNode;
};

export default function PageHeader({ action, description, title }: PageHeaderProps) {
    return (
        <div className="flex flex-wrap items-start justify-between gap-3">
            <div>
                <h1 className="text-2xl font-semibold tracking-tight text-slate-900">{title}</h1>
                {description ? <p className="mt-1 text-sm text-slate-600">{description}</p> : null}
            </div>
            {action ? <div>{action}</div> : null}
        </div>
    );
}
