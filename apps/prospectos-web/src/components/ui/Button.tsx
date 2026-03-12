import type { ButtonHTMLAttributes, ReactNode } from 'react';
import { useTranslation } from 'react-i18next';

import { cn } from '../../utils/cn';

type ButtonVariant = 'primary' | 'secondary' | 'ghost';

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
    children: ReactNode;
    loading?: boolean;
    variant?: ButtonVariant;
};

const variantClass: Record<ButtonVariant, string> = {
    primary: 'bg-blue-600 text-white hover:bg-blue-700',
    secondary: 'border border-slate-300 bg-white text-slate-800 hover:bg-slate-100',
    ghost: 'bg-transparent text-slate-700 hover:bg-slate-100',
};

export default function Button({ children, className, disabled, loading = false, type = 'button', variant = 'primary', ...props }: ButtonProps) {
    const { t } = useTranslation();

    return (
        <button
            type={type}
            className={cn(
                'inline-flex items-center justify-center rounded-lg px-4 py-2 text-sm font-medium transition',
                'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 focus-visible:ring-offset-2',
                'disabled:cursor-not-allowed disabled:opacity-60',
                variantClass[variant],
                className
            )}
            disabled={disabled || loading}
            {...props}
        >
            {loading ? t('common.loading') : children}
        </button>
    );
}
