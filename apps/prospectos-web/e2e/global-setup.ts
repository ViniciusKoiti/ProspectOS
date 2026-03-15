const defaultHealthUrl = 'http://127.0.0.1:8080/actuator/health';

function sleep(milliseconds: number): Promise<void> {
    return new Promise((resolve) => {
        setTimeout(resolve, milliseconds);
    });
}

async function waitForBackendHealth(healthUrl: string): Promise<void> {
    const timeoutAt = Date.now() + 90_000;

    while (Date.now() < timeoutAt) {
        try {
            const response = await fetch(healthUrl);
            if (response.ok) {
                return;
            }
        } catch {
            // Keep polling until timeout.
        }

        await sleep(1_500);
    }

    throw new Error(
        `[e2e] Backend is not healthy at ${healthUrl}. Start backend before running Playwright tests.`
    );
}

async function globalSetup(): Promise<void> {
    const healthUrl = process.env.E2E_BACKEND_HEALTH_URL ?? defaultHealthUrl;
    await waitForBackendHealth(healthUrl);
}

export default globalSetup;
