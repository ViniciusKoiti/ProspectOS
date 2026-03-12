import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Navigate, Route, Routes } from 'react-router-dom';

import MainLayout from './components/layout/MainLayout';
import CompaniesPage from './pages/CompaniesPage';
import CompanyDetailPage from './pages/CompanyDetailPage';
import DashboardPage from './pages/DashboardPage';
import IcpsPage from './pages/IcpsPage';
import SearchPage from './pages/SearchPage';

const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            staleTime: 5 * 60 * 1000,
            retry: 1,
            refetchOnWindowFocus: false,
        },
    },
});

export default function App() {
    return (
        <QueryClientProvider client={queryClient}>
            <Routes>
                <Route element={<MainLayout />}>
                    <Route path="/" element={<DashboardPage />} />
                    <Route path="/search" element={<SearchPage />} />
                    <Route path="/icps" element={<IcpsPage />} />
                    <Route path="/companies" element={<CompaniesPage />} />
                    <Route path="/companies/:id" element={<CompanyDetailPage />} />
                    <Route path="*" element={<Navigate to="/" replace />} />
                </Route>
            </Routes>
        </QueryClientProvider>
    );
}
