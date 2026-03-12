import { type LeadSearchRequest, leadSearchRequestSchema, type LeadSearchResponse,leadSearchResponseSchema } from '../types/contracts';
import { api } from './api';

export async function searchLeads(payload: LeadSearchRequest): Promise<LeadSearchResponse> {
    const parsedPayload = leadSearchRequestSchema.parse(payload);
    const response = await api.post('/leads/search', parsedPayload);
    return leadSearchResponseSchema.parse(response.data);
}
