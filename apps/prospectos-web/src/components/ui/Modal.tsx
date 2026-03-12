import { XMarkIcon } from '@heroicons/react/24/outline';
import { type ReactNode,useEffect } from 'react';
import { useTranslation } from 'react-i18next';

import { cn } from '../../utils/cn';

type ModalProps = {
    open: boolean;
    title: string;
    description?: string;
    onClose: () => void;
    children: ReactNode;
    footer?: ReactNode;
    size?: 'md' | 'lg';
};

const sizeClass = {
    md: 'max-w-2xl',
    lg: 'max-w-4xl',
} as const;

export default function Modal({ children, description, footer, onClose, open, size = 'md', title }: ModalProps) {
    const { t } = useTranslation();

    useEffect(() => {
        if (!open) {
            return undefined;
        }

        const previousOverflow = document.body.style.overflow;
        document.body.style.overflow = 'hidden';

        const onKeyDown = (event: KeyboardEvent) => {
            if (event.key === 'Escape') {
                onClose();
            }
        };

        window.addEventListener('keydown', onKeyDown);

        return () => {
            document.body.style.overflow = previousOverflow;
            window.removeEventListener('keydown', onKeyDown);
        };
    }, [onClose, open]);

    if (!open) {
        return null;
    }

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/45 p-4" onClick={onClose} role="presentation">
            <div
                aria-modal="true"
                className={cn('w-full overflow-hidden rounded-2xl bg-white shadow-2xl', sizeClass[size])}
                onClick={(event) => event.stopPropagation()}
                role="dialog"
            >
                <div className="flex items-start justify-between gap-4 border-b border-slate-200 px-6 py-5">
                    <div className="space-y-1">
                        <h2 className="text-lg font-semibold text-slate-900">{title}</h2>
                        {description ? <p className="text-sm text-slate-600">{description}</p> : null}
                    </div>
                    <button
                        aria-label={t('ui.modal.close')}
                        className="rounded-lg p-2 text-slate-500 transition hover:bg-slate-100 hover:text-slate-700"
                        onClick={onClose}
                        type="button"
                    >
                        <XMarkIcon className="h-5 w-5" />
                    </button>
                </div>
                <div className="max-h-[70vh] overflow-y-auto px-6 py-5">{children}</div>
                {footer ? <div className="border-t border-slate-200 px-6 py-4">{footer}</div> : null}
            </div>
        </div>
    );
}
