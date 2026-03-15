import { createElement } from 'react';
import { renderToStaticMarkup } from 'react-dom/server';
import { describe, expect, it, vi } from 'vitest';

import CompanyContactsSection from '../CompanyContactsSection';

const translations: Record<string, string> = {
    'common.retry': 'Tentar novamente',
    'ui.error.title': 'Algo deu errado',
    'pages.companyDetail.contacts.loading': 'Carregando contatos...',
    'pages.companyDetail.contacts.errors.load': 'Falha ao carregar contatos da empresa.',
    'pages.companyDetail.contacts.empty.title': 'Nenhum contato cadastrado',
    'pages.companyDetail.contacts.empty.description': 'Ainda nao ha pessoas vinculadas para esta empresa.',
    'pages.companyDetail.contacts.table.name': 'Nome',
    'pages.companyDetail.contacts.table.position': 'Cargo',
    'pages.companyDetail.contacts.table.email': 'Email',
    'pages.companyDetail.contacts.table.phoneNumber': 'Telefone',
};

function translate(key: string): string {
    return translations[key] ?? key;
}

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: translate,
    }),
}));

describe('CompanyContactsSection', () => {
    it('renders loading state', () => {
        const markup = renderToStaticMarkup(
            createElement(CompanyContactsSection, {
                contacts: undefined,
                isLoading: true,
                isError: false,
                onRetry: () => undefined,
            }),
        );

        expect(markup).toContain('Carregando contatos...');
    });

    it('renders error state', () => {
        const markup = renderToStaticMarkup(
            createElement(CompanyContactsSection, {
                contacts: undefined,
                isLoading: false,
                isError: true,
                onRetry: () => undefined,
            }),
        );

        expect(markup).toContain('Algo deu errado');
        expect(markup).toContain('Falha ao carregar contatos da empresa.');
        expect(markup).toContain('Tentar novamente');
    });

    it('renders empty state', () => {
        const markup = renderToStaticMarkup(
            createElement(CompanyContactsSection, {
                contacts: [],
                isLoading: false,
                isError: false,
                onRetry: () => undefined,
            }),
        );

        expect(markup).toContain('Nenhum contato cadastrado');
        expect(markup).toContain('Ainda nao ha pessoas vinculadas para esta empresa.');
    });

    it('renders table with contacts', () => {
        const markup = renderToStaticMarkup(
            createElement(CompanyContactsSection, {
                contacts: [
                    {
                        name: 'Maria Silva',
                        position: 'CTO',
                        email: 'maria@alpha.example',
                        phoneNumber: '+55 11 99999-9999',
                    },
                    {
                        name: 'Joao Souza',
                        position: null,
                        email: 'joao@alpha.example',
                        phoneNumber: null,
                    },
                ],
                isLoading: false,
                isError: false,
                onRetry: () => undefined,
            }),
        );

        expect(markup).toContain('Nome');
        expect(markup).toContain('Cargo');
        expect(markup).toContain('Email');
        expect(markup).toContain('Telefone');
        expect(markup).toContain('Maria Silva');
        expect(markup).toContain('maria@alpha.example');
        expect(markup).toContain('Joao Souza');
        expect(markup).toContain('joao@alpha.example');
    });
});
