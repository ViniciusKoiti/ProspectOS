import type { Icp, IcpUpsert } from '../types/icpContracts';
import { icpSchema, icpUpsertSchema } from '../types/icpContracts';
import { api } from './api';

export async function listIcps(): Promise<Icp[]> {
    const response = await api.get('/icps');
    return icpSchema.array().parse(response.data);
}

export async function createIcp(payload: IcpUpsert): Promise<Icp> {
    const response = await api.post('/icps', icpUpsertSchema.parse(payload));
    return icpSchema.parse(response.data);
}

export async function updateIcp(icpId: number, payload: IcpUpsert): Promise<Icp> {
    const response = await api.put(`/icps/${icpId}`, icpUpsertSchema.parse(payload));
    return icpSchema.parse(response.data);
}

export async function deleteIcp(icpId: number): Promise<void> {
    await api.delete(`/icps/${icpId}`);
}
