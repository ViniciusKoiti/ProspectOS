import { defineConfig, devices } from '@playwright/test';

const frontendPort = Number(process.env.E2E_FRONTEND_PORT ?? 4173);
const frontendBaseUrl = process.env.E2E_FRONTEND_URL ?? `http://127.0.0.1:${frontendPort}`;
const backendApiUrl = process.env.E2E_API_URL ?? 'http://127.0.0.1:8080/api';

export default defineConfig({
    testDir: './e2e',
    timeout: 45_000,
    fullyParallel: false,
    retries: process.env.CI ? 1 : 0,
    reporter: [['list'], ['html', { open: 'never' }]],
    globalSetup: './e2e/global-setup.ts',
    use: {
        baseURL: frontendBaseUrl,
        trace: 'on-first-retry',
        screenshot: 'only-on-failure',
        video: 'retain-on-failure',
    },
    projects: [
        {
            name: 'chromium',
            use: { ...devices['Desktop Chrome'] },
        },
    ],
    webServer: {
        command: `pnpm dev --host 127.0.0.1 --port ${frontendPort} --strictPort`,
        cwd: __dirname,
        url: frontendBaseUrl,
        reuseExistingServer: !process.env.CI,
        timeout: 120_000,
        env: {
            VITE_API_URL: backendApiUrl,
        },
    },
});
