import type { Company, CompanyContact } from '../types/companyContracts';
import { companyContactsSchema, companySchema } from '../types/companyContracts';
import { api } from './api';

export async function listCompanies(): Promise<Company[]> {
    const response = await api.get('/companies');
    return companySchema.array().parse(response.data);
}

export async function getCompany(companyId: string): Promise<Company> {
    const response = await api.get(`/companies/${companyId}`);
    return companySchema.parse(response.data);
}

export async function getCompanyContacts(companyId: string): Promise<CompanyContact[]> {
    const response = await api.get(`/companies/${companyId}/contacts`);
    return companyContactsSchema.parse(response.data);
}
