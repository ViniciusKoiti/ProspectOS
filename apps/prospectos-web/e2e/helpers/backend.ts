import type { APIRequestContext } from '@playwright/test';

const defaultApiUrl = 'http://127.0.0.1:8080/api';

function resolveApiUrl(): string {
    return process.env.E2E_API_URL ?? defaultApiUrl;
}

async function expectOkResponse(response: Response, context: string): Promise<void> {
    if (response.ok) {
        return;
    }

    const body = await response.text();
    throw new Error(`[e2e] ${context} failed with ${response.status}: ${body}`);
}

type LeadSearchResult = {
    leadKey: string;
    candidate: unknown;
    score: unknown;
    source: unknown;
};

type CompanySeed = {
    id: string;
    name: string;
};

function asCompanySeed(payload: unknown): CompanySeed {
    if (typeof payload !== 'object' || payload === null) {
        throw new Error('[e2e] Invalid company payload.');
    }

    const asRecord = payload as Record<string, unknown>;
    const id = String(asRecord.id ?? '');
    const name = String(asRecord.name ?? '');

    if (!id || !name) {
        throw new Error('[e2e] Company payload missing id or name.');
    }

    return { id, name };
}

export async function ensureCompanySeeded(request: APIRequestContext): Promise<CompanySeed> {
    const apiUrl = resolveApiUrl();

    const companiesResponse = await request.get(`${apiUrl}/companies`);
    if (!companiesResponse.ok()) {
        const body = await companiesResponse.text();
        throw new Error(`[e2e] GET /companies failed with ${companiesResponse.status()}: ${body}`);
    }

    const companiesPayload = (await companiesResponse.json()) as unknown;
    if (Array.isArray(companiesPayload) && companiesPayload.length > 0) {
        return asCompanySeed(companiesPayload[0]);
    }

    const leadSearchResponse = await request.post(`${apiUrl}/leads/search`, {
        data: {
            query: 'empresas saas b2b no brasil',
            limit: 5,
            sources: ['in-memory'],
            icpId: null,
        },
    });

    if (!leadSearchResponse.ok()) {
        const body = await leadSearchResponse.text();
        throw new Error(`[e2e] POST /leads/search failed with ${leadSearchResponse.status()}: ${body}`);
    }

    const leadSearchPayload = (await leadSearchResponse.json()) as { leads?: LeadSearchResult[] };
    const firstLead = leadSearchPayload.leads?.[0];
    if (!firstLead) {
        throw new Error('[e2e] No leads returned to seed companies.');
    }

    const acceptLeadResponse = await request.post(`${apiUrl}/leads/accept`, {
        data: {
            leadKey: firstLead.leadKey,
            candidate: firstLead.candidate,
            score: firstLead.score,
            source: firstLead.source,
        },
    });

    if (!acceptLeadResponse.ok()) {
        const body = await acceptLeadResponse.text();
        throw new Error(`[e2e] POST /leads/accept failed with ${acceptLeadResponse.status()}: ${body}`);
    }

    const acceptLeadPayload = (await acceptLeadResponse.json()) as { company?: unknown };
    if (!acceptLeadPayload.company) {
        throw new Error('[e2e] Accept lead response missing company.');
    }

    return asCompanySeed(acceptLeadPayload.company);
}

export async function assertBackendHealth(): Promise<void> {
    const healthUrl = process.env.E2E_BACKEND_HEALTH_URL ?? 'http://127.0.0.1:8080/actuator/health';
    const response = await fetch(healthUrl);
    await expectOkResponse(response, 'Backend health check');
}
