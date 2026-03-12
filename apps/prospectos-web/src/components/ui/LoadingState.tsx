import { useTranslation } from 'react-i18next';

type LoadingStateProps = {
    label?: string;
};

export default function LoadingState({ label }: LoadingStateProps) {
    const { t } = useTranslation();

    return (
        <div className="rounded-xl border border-slate-200 bg-white p-6">
            <div className="animate-pulse space-y-3">
                <div className="h-4 w-48 rounded bg-slate-200" />
                <div className="h-4 w-full rounded bg-slate-200" />
                <div className="h-4 w-10/12 rounded bg-slate-200" />
                <div className="h-4 w-8/12 rounded bg-slate-200" />
            </div>
            <p className="mt-4 text-xs text-slate-500">{label ?? t('ui.loading.label')}</p>
        </div>
    );
}
