import {
    type AcceptLeadRequest,
    acceptLeadRequestSchema,
    type AcceptLeadResponse,
    acceptLeadResponseSchema,
    type LeadSearchAsyncSnapshot,
    leadSearchAsyncSnapshotSchema,
    type LeadSearchAsyncStartResponse,
    leadSearchAsyncStartResponseSchema,
    type LeadSearchRequest,
    leadSearchRequestSchema,
    type LeadSearchResponse,
    leadSearchResponseSchema,
} from '../types/leadContracts';
import { api } from './api';

export async function searchLeads(payload: LeadSearchRequest): Promise<LeadSearchResponse> {
    const parsedPayload = leadSearchRequestSchema.parse(payload);
    const response = await api.post('/leads/search', parsedPayload);
    return leadSearchResponseSchema.parse(response.data);
}

export async function startLeadSearchAsync(payload: LeadSearchRequest): Promise<LeadSearchAsyncStartResponse> {
    const parsedPayload = leadSearchRequestSchema.parse(payload);
    const response = await api.post('/leads/search/async', parsedPayload);
    return leadSearchAsyncStartResponseSchema.parse(response.data);
}

export async function getLeadSearchAsyncStatus(requestId: string): Promise<LeadSearchAsyncSnapshot> {
    const response = await api.get(`/leads/search/${requestId}`);
    return leadSearchAsyncSnapshotSchema.parse(response.data);
}

type LeadSearchEventHandlers = {
    onSnapshot: (snapshot: LeadSearchAsyncSnapshot) => void;
    onError?: (error: Event) => void;
};

export function openLeadSearchEvents(requestId: string, handlers: LeadSearchEventHandlers): EventSource {
    const baseUrl = api.defaults.baseURL ?? '';
    const normalizedBase = baseUrl.endsWith('/api') ? baseUrl.slice(0, -4) : baseUrl;
    const url = `${normalizedBase}/api/leads/search/${requestId}/events`;
    const stream = new EventSource(url);

    stream.addEventListener('snapshot', (event) => {
        if (!(event instanceof MessageEvent)) {
            return;
        }
        handlers.onSnapshot(leadSearchAsyncSnapshotSchema.parse(JSON.parse(event.data)));
    });

    if (handlers.onError) {
        stream.onerror = handlers.onError;
    }

    return stream;
}

export async function acceptLead(payload: AcceptLeadRequest): Promise<AcceptLeadResponse> {
    const parsedPayload = acceptLeadRequestSchema.parse(payload);
    const response = await api.post('/leads/accept', parsedPayload);
    return acceptLeadResponseSchema.parse(response.data);
}
