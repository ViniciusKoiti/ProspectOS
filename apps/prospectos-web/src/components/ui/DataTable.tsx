import type { ReactNode } from 'react';
import { useTranslation } from 'react-i18next';

import EmptyState from './EmptyState';

export type DataTableColumn<T> = {
    key: string;
    header: string;
    render: (row: T) => ReactNode;
};

type DataTableProps<T> = {
    columns: DataTableColumn<T>[];
    rows: T[];
    rowKey: (row: T, index: number) => string;
    emptyTitle?: string;
    emptyDescription?: string;
};

export default function DataTable<T>({ columns, emptyDescription, emptyTitle, rowKey, rows }: DataTableProps<T>) {
    const { t } = useTranslation();

    const resolvedEmptyTitle = emptyTitle ?? t('ui.table.noRecords');
    const resolvedEmptyDescription = emptyDescription ?? t('ui.table.noData');

    if (rows.length === 0) {
        return <EmptyState description={resolvedEmptyDescription} title={resolvedEmptyTitle} />;
    }

    return (
        <div className="overflow-hidden rounded-xl border border-slate-200 bg-white">
            <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-slate-200">
                    <thead className="bg-slate-50">
                        <tr>
                            {columns.map((column) => (
                                <th key={column.key} className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-600">
                                    {column.header}
                                </th>
                            ))}
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100 bg-white">
                        {rows.map((row, index) => (
                            <tr key={rowKey(row, index)} className="hover:bg-slate-50/80">
                                {columns.map((column) => (
                                    <td key={column.key} className="px-4 py-3 text-sm text-slate-700">
                                        {column.render(row)}
                                    </td>
                                ))}
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}
