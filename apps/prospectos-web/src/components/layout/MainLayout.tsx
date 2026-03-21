import { BuildingOffice2Icon, HomeIcon, MagnifyingGlassIcon, PaperAirplaneIcon, UserGroupIcon } from '@heroicons/react/24/outline';
import type { ComponentType, SVGProps } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, NavLink, Outlet } from 'react-router-dom';

import LanguageSwitcher from './LanguageSwitcher';

type NavigationItem = {
    label: string;
    to: string;
    icon: ComponentType<SVGProps<SVGSVGElement>>;
};

export default function MainLayout() {
    const { t } = useTranslation();

    const navigation: NavigationItem[] = [
        { label: t('nav.dashboard'), to: '/', icon: HomeIcon },
        { label: t('nav.search'), to: '/search', icon: MagnifyingGlassIcon },
        { label: t('nav.outreach', { defaultValue: 'Outreach' }), to: '/outreach', icon: PaperAirplaneIcon },
        { label: t('nav.icps'), to: '/icps', icon: UserGroupIcon },
        { label: t('nav.companies'), to: '/companies', icon: BuildingOffice2Icon },
    ];

    return (
        <div className="min-h-screen bg-slate-50 text-slate-900">
            <header className="border-b border-slate-200 bg-white">
                <div className="mx-auto flex h-16 max-w-7xl items-center justify-between gap-3 px-4 sm:px-6 lg:px-8">
                    <Link to="/" className="text-lg font-semibold tracking-tight text-blue-700">
                        {t('common.appName')}
                    </Link>
                    <div className="flex items-center gap-3">
                        <LanguageSwitcher />
                        <span className="rounded-full bg-blue-50 px-3 py-1 text-xs font-medium text-blue-700">{t('common.week2')}</span>
                    </div>
                </div>
            </header>

            <div className="mx-auto grid max-w-7xl grid-cols-1 gap-6 px-4 py-6 sm:px-6 lg:grid-cols-[220px,1fr] lg:px-8">
                <aside className="rounded-xl border border-slate-200 bg-white p-3">
                    <nav className="space-y-1">
                        {navigation.map((item) => (
                            <NavLink
                                key={item.to}
                                to={item.to}
                                end={item.to === '/'}
                                className={({ isActive }) =>
                                    [
                                        'flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors',
                                        isActive
                                            ? 'bg-blue-600 text-white'
                                            : 'text-slate-700 hover:bg-slate-100 hover:text-slate-900',
                                    ].join(' ')
                                }
                            >
                                <item.icon className="h-5 w-5" />
                                {item.label}
                            </NavLink>
                        ))}
                    </nav>
                </aside>

                <main className="space-y-4">
                    <Outlet />
                </main>
            </div>
        </div>
    );
}
