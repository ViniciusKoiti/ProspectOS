import type { Company, CompanyContact } from '../types/companyContracts';
import { companyContactsSchema, companySchema } from '../types/companyContracts';
import { api } from './api';

export type CompanyListParams = {
    query?: string;
    industry?: string;
    location?: string;
    minScore?: number;
    maxScore?: number;
    hasContact?: boolean;
    page?: number;
    size?: number;
};

export type CompanyListResponse = {
    items: Company[];
    page: number;
    size: number;
    totalItems: number;
    totalPages: number;
};

type CompanyListPayload = {
    items?: unknown;
    content?: unknown;
    page?: unknown;
    size?: unknown;
    totalItems?: unknown;
    totalElements?: unknown;
    totalPages?: unknown;
};

const DEFAULT_PAGE_SIZE = 10;

function normalizeTextParam(value: string | undefined): string | undefined {
    const normalized = value?.trim();
    return normalized ? normalized : undefined;
}

function parseOptionalNumber(value: unknown): number | null {
    if (typeof value === 'number' && Number.isFinite(value)) {
        return value;
    }

    if (typeof value === 'string') {
        const parsed = Number(value);
        return Number.isFinite(parsed) ? parsed : null;
    }

    return null;
}

function parseTotalCountHeader(rawHeader: unknown): number | null {
    if (typeof rawHeader === 'string') {
        const parsed = Number(rawHeader);
        return Number.isFinite(parsed) ? parsed : null;
    }

    if (Array.isArray(rawHeader) && rawHeader.length > 0) {
        return parseTotalCountHeader(rawHeader[0]);
    }

    return null;
}

function readHeaderCaseInsensitive(headers: unknown, headerName: string): unknown {
    if (!headers || typeof headers !== 'object') {
        return null;
    }

    const getter = (headers as { get?: (name: string) => unknown }).get;
    if (typeof getter === 'function') {
        const getterValue = getter.call(headers, headerName);
        if (getterValue !== undefined) {
            return getterValue;
        }
    }

    const loweredHeaderName = headerName.toLowerCase();
    for (const [key, value] of Object.entries(headers as Record<string, unknown>)) {
        if (key.toLowerCase() === loweredHeaderName) {
            return value;
        }
    }

    return null;
}

function buildQueryParams(params: CompanyListParams): Record<string, string | number | boolean> {
    const queryParams: Record<string, string | number | boolean> = {};

    const query = normalizeTextParam(params.query);
    if (query) {
        queryParams.query = query;
    }

    const industry = normalizeTextParam(params.industry);
    if (industry) {
        queryParams.industry = industry;
    }

    const location = normalizeTextParam(params.location);
    if (location) {
        queryParams.location = location;
    }

    if (typeof params.minScore === 'number' && Number.isFinite(params.minScore)) {
        queryParams.minScore = params.minScore;
    }

    if (typeof params.maxScore === 'number' && Number.isFinite(params.maxScore)) {
        queryParams.maxScore = params.maxScore;
    }

    if (typeof params.hasContact === 'boolean') {
        queryParams.hasContact = params.hasContact;
    }

    if (typeof params.page === 'number' && Number.isInteger(params.page) && params.page >= 0) {
        queryParams.page = params.page;
    }

    if (typeof params.size === 'number' && Number.isInteger(params.size) && params.size > 0) {
        queryParams.size = params.size;
    }

    return queryParams;
}

function parseCompaniesPayload(payload: unknown): Company[] {
    return companySchema.array().parse(payload);
}

function parsePagedResponse(
    payload: CompanyListPayload,
    fallbackPage: number,
    fallbackSize: number,
    totalCountHeader: number | null
): CompanyListResponse {
    const itemsPayload = payload.items ?? payload.content ?? [];
    const items = parseCompaniesPayload(itemsPayload);

    const page = parseOptionalNumber(payload.page) ?? fallbackPage;
    const size = parseOptionalNumber(payload.size) ?? fallbackSize;

    const totalItems =
        parseOptionalNumber(payload.totalItems)
        ?? parseOptionalNumber(payload.totalElements)
        ?? totalCountHeader
        ?? items.length;

    const totalPages =
        parseOptionalNumber(payload.totalPages)
        ?? (size > 0 ? Math.max(1, Math.ceil(totalItems / size)) : 1);

    return {
        items,
        page,
        size,
        totalItems,
        totalPages,
    };
}

function parseLegacyResponse(
    payload: unknown,
    fallbackPage: number,
    fallbackSize: number,
    totalCountHeader: number | null
): CompanyListResponse {
    const items = parseCompaniesPayload(payload);
    const totalItems = totalCountHeader ?? items.length;
    const totalPages = fallbackSize > 0 ? Math.max(1, Math.ceil(totalItems / fallbackSize)) : 1;

    return {
        items,
        page: fallbackPage,
        size: fallbackSize,
        totalItems,
        totalPages,
    };
}

export async function listCompanies(params: CompanyListParams = {}): Promise<CompanyListResponse> {
    const fallbackPage = params.page ?? 0;
    const fallbackSize = params.size ?? DEFAULT_PAGE_SIZE;

    const response = await api.get('/companies', {
        params: buildQueryParams(params),
    });

    const totalCountHeader = parseTotalCountHeader(readHeaderCaseInsensitive(response.headers, 'x-total-count'));

    if (Array.isArray(response.data)) {
        return parseLegacyResponse(response.data, fallbackPage, fallbackSize, totalCountHeader);
    }

    return parsePagedResponse(response.data as CompanyListPayload, fallbackPage, fallbackSize, totalCountHeader);
}

export async function getCompany(companyId: string): Promise<Company> {
    const response = await api.get(`/companies/${companyId}`);
    return companySchema.parse(response.data);
}

export async function getCompanyContacts(companyId: string): Promise<CompanyContact[]> {
    const response = await api.get(`/companies/${companyId}/contacts`);
    return companyContactsSchema.parse(response.data);
}
