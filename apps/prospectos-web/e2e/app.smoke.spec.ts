import { expect, test } from '@playwright/test';

import { assertBackendHealth, ensureCompanySeeded } from './helpers/backend';

test.describe.configure({ mode: 'serial' });

test.beforeAll(async ({ request }) => {
    await assertBackendHealth();
    await ensureCompanySeeded(request);
});

test.beforeEach(async ({ page }) => {
    await page.addInitScript(() => {
        window.localStorage.setItem('prospectos-web-language', 'pt-BR');
    });
});

test('navigates through primary pages', async ({ page }) => {
    await page.goto('/');
    await expect(page.getByTestId('dashboard-page')).toBeVisible();

    await page.locator('a[href="/search"]').click();
    await expect(page).toHaveURL(/\/search$/);
    await expect(page.getByTestId('search-page')).toBeVisible();

    await page.locator('a[href="/icps"]').click();
    await expect(page).toHaveURL(/\/icps$/);
    await expect(page.getByTestId('icps-page')).toBeVisible();

    await page.locator('a[href="/companies"]').click();
    await expect(page).toHaveURL(/\/companies$/);
    await expect(page.getByTestId('companies-page')).toBeVisible();
});

test('executes lead search and accepts one lead', async ({ page }) => {
    await page.goto('/search');

    await page.fill('#search-query', 'empresas b2b de software no brasil');
    await page.selectOption('#search-limit', '10');

    const searchResponsePromise = page.waitForResponse((response) => {
        return response.url().includes('/api/leads/search') && response.request().method() === 'POST';
    });

    await page.locator('[data-testid="search-form"] button[type="submit"]').click();

    const searchResponse = await searchResponsePromise;
    expect(searchResponse.ok()).toBeTruthy();

    await expect(page.getByTestId('search-results-table')).toBeVisible();
    const acceptButton = page.locator('[data-testid^="accept-lead-"]').first();
    await expect(acceptButton).toBeVisible();

    const acceptResponsePromise = page.waitForResponse((response) => {
        return response.url().includes('/api/leads/accept') && response.request().method() === 'POST';
    });

    await acceptButton.click();

    const acceptResponse = await acceptResponsePromise;
    expect(acceptResponse.ok()).toBeTruthy();

    await expect(page.getByTestId('search-accept-feedback')).toBeVisible();
    await page.getByTestId('search-view-accepted-company').click();

    await expect(page).toHaveURL(/\/companies\/[-0-9]+$/);
    await expect(page.getByTestId('company-detail-page')).toBeVisible();
});

test('opens company detail from company list', async ({ page }) => {
    await page.goto('/companies');
    await expect(page.getByTestId('companies-page')).toBeVisible();

    const companyLink = page.locator('[data-testid^="company-link-"]').first();
    await expect(companyLink).toBeVisible();
    await companyLink.click();

    await expect(page).toHaveURL(/\/companies\/[-0-9]+$/);
    await expect(page.getByTestId('company-detail-page')).toBeVisible();
});
