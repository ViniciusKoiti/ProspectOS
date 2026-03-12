  import {
    type AcceptLeadRequest,
    acceptLeadRequestSchema,
    type AcceptLeadResponse,
    acceptLeadResponseSchema,
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

export async function acceptLead(payload: AcceptLeadRequest): Promise<AcceptLeadResponse> {
    const parsedPayload = acceptLeadRequestSchema.parse(payload);
    const response = await api.post('/leads/accept', parsedPayload);
    return acceptLeadResponseSchema.parse(response.data);
}
