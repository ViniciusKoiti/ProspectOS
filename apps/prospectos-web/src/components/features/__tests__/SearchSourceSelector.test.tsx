import { createElement } from 'react';
import { renderToStaticMarkup } from 'react-dom/server';
import { describe, expect, it, vi } from 'vitest';

import SearchSourceSelector, { type SearchSourceOption } from '../SearchSourceSelector';

const OPTIONS: SearchSourceOption[] = [
    {
        value: 'in-memory',
        label: 'in-memory',
        description: 'Mock source',
    },
    {
        value: 'vector-company',
        label: 'vector-company',
        description: 'Semantic source',
    },
    {
        value: 'cnpj-ws',
        label: 'cnpj-ws',
        description: 'CNPJ source',
    },
];

describe('SearchSourceSelector', () => {
    it('renders label, hint and selected source state', () => {
        const onChange = vi.fn();

        const markup = renderToStaticMarkup(
            createElement(SearchSourceSelector, {
                label: 'Sources',
                hint: 'Choose one or more sources.',
                selectedSources: ['in-memory'],
                options: OPTIONS,
                onChange,
            })
        );

        expect(markup).toContain('Sources');
        expect(markup).toContain('Choose one or more sources.');
        expect(markup).toContain('in-memory');
        expect(markup).toContain('vector-company');
        expect(markup).toContain('cnpj-ws');
        expect(markup).toContain('checked=""');
    });

    it('disables all checkboxes when component is disabled', () => {
        const markup = renderToStaticMarkup(
            createElement(SearchSourceSelector, {
                label: 'Sources',
                selectedSources: ['in-memory', 'vector-company'],
                options: OPTIONS,
                disabled: true,
                onChange: vi.fn(),
            })
        );

        expect(markup).toContain('disabled=""');
    });
});
