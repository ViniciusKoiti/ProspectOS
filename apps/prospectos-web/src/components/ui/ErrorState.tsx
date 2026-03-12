import { useTranslation } from 'react-i18next';

import Button from './Button';

type ErrorStateProps = {
    message: string;
    onRetry?: () => void;
};

export default function ErrorState({ message, onRetry }: ErrorStateProps) {
    const { t } = useTranslation();

    return (
        <div className="rounded-xl border border-red-200 bg-red-50 p-6">
            <h3 className="text-base font-semibold text-red-800">{t('ui.error.title')}</h3>
            <p className="mt-1 text-sm text-red-700">{message}</p>
            {onRetry ? (
                <div className="mt-4">
                    <Button onClick={onRetry} variant="secondary">
                        {t('common.retry')}
                    </Button>
                </div>
            ) : null}
        </div>
    );
}
