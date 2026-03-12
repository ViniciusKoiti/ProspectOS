import { type Company,companySchema } from '../types/contracts';
import { api } from './api';

export async function listCompanies(): Promise<Company[]> {
    const response = await api.get('/companies');
    return companySchema.array().parse(response.data);
}

export async function getCompany(companyId: number): Promise<Company> {
    const response = await api.get(`/companies/${companyId}`);
    return companySchema.parse(response.data);
}
