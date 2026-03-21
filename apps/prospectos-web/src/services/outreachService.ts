import type { OutreachCampaignRequest, OutreachCampaignResponse } from '../types/outreachContracts';
import { outreachCampaignRequestSchema, outreachCampaignResponseSchema } from '../types/outreachContracts';
import { api } from './api';

export async function startOutreachCampaign(payload: OutreachCampaignRequest): Promise<OutreachCampaignResponse> {
    const parsedPayload = outreachCampaignRequestSchema.parse(payload);
    const response = await api.post('/outreach/campaigns', parsedPayload);
    return outreachCampaignResponseSchema.parse(response.data);
}
