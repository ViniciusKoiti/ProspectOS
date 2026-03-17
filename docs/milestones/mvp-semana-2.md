# 🎨 MVP SEMANA 2 - Frontend Essencial

**Período:** 5 dias úteis  
**Objetivo:** Interface web que permita usar o produto  
**Meta:** Usuário consegue fazer buscas e ver resultados via browser  
**Dependência:** ✅ Semana 1 concluída (backend funcionando)

---

## 🎯 OVERVIEW DA SEMANA

### **Por que Frontend agora?**
Com backend estável (Semana 1), chegou a hora de tornar o produto usável por não-desenvolvedores:
- 🖥️ Interface web intuitiva
- 📱 Experiência mobile-friendly
- 🔗 Integração completa com APIs REST
- 🎪 Demo-ready para prospects

### **Stack Tecnológico:**
- **React 18** + TypeScript (type safety)
- **Vite** (build rápido vs Create React App)
- **TailwindCSS** (UI sem designer)
- **React Query** (cache + API calls)
- **React Hook Form** (forms performáticos)

### **O que você terá no final:**
- 4 telas principais funcionando
- Integração completa com backend
- Interface responsiva e profissional
- Loading states e error handling
- Produto usável por SDRs reais

---

## 📅 CRONOGRAMA DETALHADO

### **DIA 1: SCAFFOLDING + SETUP**

#### **🏗️ Setup Inicial (Morning)**

**1. Criar projeto React:**
```bash
# No diretório raiz do projeto
npm create vite@latest prospectos-web -- --template react-ts
cd prospectos-web

# Instalar dependências essenciais
npm install
npm install @tailwindcss/forms @tailwindcss/typography
npm install @tanstack/react-query axios
npm install react-hook-form @hookform/resolvers
npm install @heroicons/react react-router-dom
npm install zod # para validação de schemas
```

**2. Configurar TailwindCSS:**
```bash
npm install -D tailwindcss postcss autoprefixer
npx tailwindcss init -p
```

```javascript
// tailwind.config.js
/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#eff6ff',
          500: '#3b82f6',
          600: '#2563eb',
          700: '#1d4ed8',
        }
      }
    },
  },
  plugins: [
    require('@tailwindcss/forms'),
    require('@tailwindcss/typography'),
  ],
}
```

```css
/* src/index.css */
@tailwind base;
@tailwind components;
@tailwind utilities;

@layer base {
  body {
    font-family: 'Inter', system-ui, sans-serif;
  }
}

@layer components {
  .btn-primary {
    @apply bg-primary-600 text-white px-4 py-2 rounded-md hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-primary-500 transition-colors;
  }
  
  .btn-secondary {
    @apply bg-gray-200 text-gray-700 px-4 py-2 rounded-md hover:bg-gray-300 focus:outline-none focus:ring-2 focus:ring-gray-500 transition-colors;
  }
}
```

#### **🔧 Configuração Arquitetura (Afternoon)**

**3. Estrutura de pastas:**
```
src/
├── components/          # Componentes reutilizáveis
│   ├── ui/             # Buttons, inputs, etc
│   ├── layout/         # Header, sidebar, etc  
│   └── features/       # Feature-specific components
├── pages/              # Páginas/telas principais
├── hooks/              # Custom hooks
├── services/           # API calls
├── types/              # TypeScript interfaces
├── utils/              # Utilitários
└── App.tsx
```

**4. Setup React Router:**
```tsx
// src/App.tsx
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import Layout from './components/layout/Layout';
import Dashboard from './pages/Dashboard';
import Search from './pages/Search';
import ICPs from './pages/ICPs';
import Companies from './pages/Companies';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000, // 5 minutes
      retry: 1,
    },
  },
});

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <Router>
        <Layout>
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/search" element={<Search />} />
            <Route path="/icps" element={<ICPs />} />
            <Route path="/companies" element={<Companies />} />
            <Route path="/companies/:id" element={<CompanyDetail />} />
          </Routes>
        </Layout>
      </Router>
    </QueryClientProvider>
  );
}

export default App;
```

**5. API Service Setup:**
```typescript
// src/services/api.ts
import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000, // 10 seconds
});

// Request interceptor for auth (future)
api.interceptors.request.use((config) => {
  // Add auth token here when implemented
  return config;
});

// Response interceptor for errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error);
    return Promise.reject(error);
  }
);
```

### **DIA 2: LAYOUT + DASHBOARD**

#### **🎨 Layout Básico**

**1. Header Component:**
```tsx
// src/components/layout/Header.tsx
import { MagnifyingGlassIcon, UserCircleIcon } from '@heroicons/react/24/outline';

export default function Header() {
  return (
    <header className="bg-white shadow-sm border-b border-gray-200">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <div className="flex items-center">
            <h1 className="text-2xl font-bold text-primary-600">ProspectOS</h1>
            <span className="ml-2 text-sm text-gray-500">MVP</span>
          </div>
          
          <nav className="hidden md:flex space-x-8">
            <a href="/" className="text-gray-700 hover:text-primary-600">Dashboard</a>
            <a href="/search" className="text-gray-700 hover:text-primary-600">Busca</a>
            <a href="/icps" className="text-gray-700 hover:text-primary-600">ICPs</a>
          </nav>
          
          <div className="flex items-center">
            <button className="p-2 text-gray-600 hover:text-primary-600">
              <MagnifyingGlassIcon className="h-5 w-5" />
            </button>
            <button className="ml-3 p-2 text-gray-600 hover:text-primary-600">
              <UserCircleIcon className="h-5 w-5" />
            </button>
          </div>
        </div>
      </div>
    </header>
  );
}
```

**2. Sidebar Navigation:**
```tsx
// src/components/layout/Sidebar.tsx
import { 
  HomeIcon, 
  MagnifyingGlassIcon, 
  BuildingOfficeIcon,
  UserGroupIcon 
} from '@heroicons/react/24/outline';

const navigation = [
  { name: 'Dashboard', href: '/', icon: HomeIcon },
  { name: 'Nova Busca', href: '/search', icon: MagnifyingGlassIcon },
  { name: 'ICPs', href: '/icps', icon: UserGroupIcon },
  { name: 'Empresas', href: '/companies', icon: BuildingOfficeIcon },
];

export default function Sidebar() {
  return (
    <div className="hidden md:flex md:w-64 md:flex-col">
      <div className="flex flex-col flex-grow pt-5 bg-primary-700 overflow-y-auto">
        <nav className="flex-1 px-2 pb-4 space-y-1">
          {navigation.map((item) => (
            <a
              key={item.name}
              href={item.href}
              className="text-primary-100 hover:bg-primary-600 group flex items-center px-2 py-2 text-sm font-medium rounded-md"
            >
              <item.icon className="text-primary-300 mr-3 h-5 w-5" />
              {item.name}
            </a>
          ))}
        </nav>
      </div>
    </div>
  );
}
```

#### **📊 Dashboard Page**

**3. Dashboard com métricas básicas:**
```tsx
// src/pages/Dashboard.tsx
import { useQuery } from '@tanstack/react-query';
import { api } from '../services/api';
import StatsCard from '../components/ui/StatsCard';
import RecentSearches from '../components/features/RecentSearches';

export default function Dashboard() {
  const { data: companies } = useQuery({
    queryKey: ['companies'],
    queryFn: () => api.get('/companies').then(res => res.data),
  });

  const { data: icps } = useQuery({
    queryKey: ['icps'],
    queryFn: () => api.get('/icps').then(res => res.data),
  });

  const stats = [
    {
      title: 'Total Empresas',
      value: companies?.length || 0,
      change: '+12%',
      changeType: 'increase' as const,
    },
    {
      title: 'ICPs Ativos',
      value: icps?.length || 0,
      change: '+3',
      changeType: 'increase' as const,
    },
    {
      title: 'Buscas Hoje',
      value: 8, // Mock data
      change: '+25%',
      changeType: 'increase' as const,
    },
    {
      title: 'Taxa Qualificação',
      value: '87%',
      change: '+5%',
      changeType: 'increase' as const,
    },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Dashboard</h1>
        <p className="mt-1 text-sm text-gray-600">
          Visão geral das suas atividades de prospecção
        </p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
        {stats.map((stat) => (
          <StatsCard key={stat.title} {...stat} />
        ))}
      </div>

      {/* Quick Actions */}
      <div className="bg-white shadow rounded-lg p-6">
        <h2 className="text-lg font-medium text-gray-900 mb-4">Ações Rápidas</h2>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
          <a
            href="/search"
            className="btn-primary text-center"
          >
            Nova Busca
          </a>
          <a
            href="/icps"
            className="btn-secondary text-center"
          >
            Gerenciar ICPs
          </a>
          <button className="btn-secondary">
            Exportar Dados
          </button>
        </div>
      </div>

      {/* Recent Activity */}
      <RecentSearches />
    </div>
  );
}
```

### **DIA 3: TELA DE BUSCA**

#### **🔍 Formulário de Busca**

**1. Search Page:**
```tsx
// src/pages/Search.tsx
import { useState } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import SearchForm from '../components/features/SearchForm';
import SearchResults from '../components/features/SearchResults';
import { searchLeads } from '../services/leadService';

const searchSchema = z.object({
  query: z.string().min(3, 'Query deve ter pelo menos 3 caracteres'),
  icpId: z.number().optional(),
  limit: z.number().min(1).max(100).default(20),
  sources: z.array(z.string()).optional(),
});

type SearchFormData = z.infer<typeof searchSchema>;

export default function Search() {
  const [searchResults, setSearchResults] = useState(null);

  const { data: icps } = useQuery({
    queryKey: ['icps'],
    queryFn: () => api.get('/icps').then(res => res.data),
  });

  const searchMutation = useMutation({
    mutationFn: searchLeads,
    onSuccess: (data) => {
      setSearchResults(data);
    },
    onError: (error) => {
      console.error('Search failed:', error);
    },
  });

  const form = useForm<SearchFormData>({
    resolver: zodResolver(searchSchema),
    defaultValues: {
      query: '',
      limit: 20,
    },
  });

  const onSubmit = (data: SearchFormData) => {
    searchMutation.mutate(data);
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Busca de Prospects</h1>
        <p className="mt-1 text-sm text-gray-600">
          Encontre prospects qualificados usando IA
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Search Form */}
        <div className="lg:col-span-1">
          <SearchForm
            form={form}
            onSubmit={onSubmit}
            icps={icps || []}
            isLoading={searchMutation.isPending}
          />
        </div>

        {/* Results */}
        <div className="lg:col-span-2">
          <SearchResults
            results={searchResults}
            isLoading={searchMutation.isPending}
            error={searchMutation.error}
          />
        </div>
      </div>
    </div>
  );
}
```

**2. Search Form Component:**
```tsx
// src/components/features/SearchForm.tsx
import { UseFormReturn } from 'react-hook-form';

interface SearchFormProps {
  form: UseFormReturn<SearchFormData>;
  onSubmit: (data: SearchFormData) => void;
  icps: any[];
  isLoading: boolean;
}

export default function SearchForm({ form, onSubmit, icps, isLoading }: SearchFormProps) {
  const { register, handleSubmit, formState: { errors } } = form;

  return (
    <div className="bg-white shadow rounded-lg p-6">
      <h2 className="text-lg font-medium text-gray-900 mb-4">Parâmetros de Busca</h2>
      
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        {/* Query Input */}
        <div>
          <label htmlFor="query" className="block text-sm font-medium text-gray-700">
            O que você procura?
          </label>
          <textarea
            {...register('query')}
            rows={3}
            placeholder="Ex: CTOs de startups fintech em São Paulo"
            className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500"
          />
          {errors.query && (
            <p className="mt-1 text-sm text-red-600">{errors.query.message}</p>
          )}
        </div>

        {/* ICP Selection */}
        <div>
          <label htmlFor="icpId" className="block text-sm font-medium text-gray-700">
            Perfil de Cliente Ideal
          </label>
          <select
            {...register('icpId', { valueAsNumber: true })}
            className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500"
          >
            <option value="">Selecione um ICP</option>
            {icps.map((icp) => (
              <option key={icp.id} value={icp.id}>
                {icp.name}
              </option>
            ))}
          </select>
        </div>

        {/* Limit */}
        <div>
          <label htmlFor="limit" className="block text-sm font-medium text-gray-700">
            Limite de Resultados
          </label>
          <select
            {...register('limit', { valueAsNumber: true })}
            className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500"
          >
            <option value={10}>10 resultados</option>
            <option value={20}>20 resultados</option>
            <option value={50}>50 resultados</option>
            <option value={100}>100 resultados</option>
          </select>
        </div>

        {/* Sources */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Fontes de Dados
          </label>
          <div className="space-y-2">
            {['in-memory', 'vector-company', 'cnpj-ws'].map((source) => (
              <label key={source} className="flex items-center">
                <input
                  type="checkbox"
                  {...register('sources')}
                  value={source}
                  className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                />
                <span className="ml-2 text-sm text-gray-700">
                  {source === 'in-memory' && 'Base Interna'}
                  {source === 'vector-company' && 'Busca Semântica'}
                  {source === 'cnpj-ws' && 'CNPJ Oficial'}
                </span>
              </label>
            ))}
          </div>
        </div>

        <button
          type="submit"
          disabled={isLoading}
          className="w-full btn-primary disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {isLoading ? 'Buscando...' : 'Buscar Prospects'}
        </button>
      </form>
    </div>
  );
}
```

### **DIA 4: RESULTADOS + ICP MANAGEMENT**

#### **📋 Search Results Component**

**1. Results Table:**
```tsx
// src/components/features/SearchResults.tsx
import { ChevronRightIcon, StarIcon } from '@heroicons/react/24/outline';

interface SearchResultsProps {
  results: any;
  isLoading: boolean;
  error: any;
}

export default function SearchResults({ results, isLoading, error }: SearchResultsProps) {
  if (isLoading) {
    return <SearchLoadingSkeleton />;
  }

  if (error) {
    return <SearchError error={error} />;
  }

  if (!results?.leads?.length) {
    return <EmptyResults />;
  }

  return (
    <div className="bg-white shadow rounded-lg">
      <div className="px-6 py-4 border-b border-gray-200">
        <h2 className="text-lg font-medium text-gray-900">
          {results.leads.length} prospects encontrados
        </h2>
        <p className="mt-1 text-sm text-gray-600">
          Ordenados por score de qualificação
        </p>
      </div>

      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Empresa
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Score
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Contato
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Fonte
              </th>
              <th className="relative px-6 py-3">
                <span className="sr-only">Actions</span>
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {results.leads.map((lead: any, index: number) => (
              <tr key={index} className="hover:bg-gray-50">
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="flex items-center">
                    <div>
                      <div className="text-sm font-medium text-gray-900">
                        {lead.company?.name || 'Nome não disponível'}
                      </div>
                      <div className="text-sm text-gray-500">
                        {lead.company?.industry} • {lead.company?.city}
                      </div>
                    </div>
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="flex items-center">
                    <StarIcon className="h-4 w-4 text-yellow-400 mr-1" />
                    <span className="text-sm font-medium text-gray-900">
                      {lead.score?.value || 0}/100
                    </span>
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {lead.company?.contacts?.[0] || 'Não disponível'}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <span className="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-primary-100 text-primary-800">
                    {lead.sourceProvenance?.sourceName}
                  </span>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                  <button className="text-primary-600 hover:text-primary-900">
                    <ChevronRightIcon className="h-5 w-5" />
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Export Button */}
      <div className="px-6 py-4 bg-gray-50 border-t border-gray-200">
        <button className="btn-secondary">
          Exportar CSV
        </button>
      </div>
    </div>
  );
}
```

#### **👥 ICP Management Page**

**2. ICPs Page:**
```tsx
// src/pages/ICPs.tsx
import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { PlusIcon } from '@heroicons/react/24/outline';
import ICPCard from '../components/features/ICPCard';
import ICPModal from '../components/features/ICPModal';
import { api } from '../services/api';

export default function ICPs() {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingICP, setEditingICP] = useState(null);
  const queryClient = useQueryClient();

  const { data: icps, isLoading } = useQuery({
    queryKey: ['icps'],
    queryFn: () => api.get('/icps').then(res => res.data),
  });

  const createMutation = useMutation({
    mutationFn: (data: any) => api.post('/icps', data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['icps'] });
      setIsModalOpen(false);
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, ...data }: any) => api.put(`/icps/${id}`, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['icps'] });
      setIsModalOpen(false);
      setEditingICP(null);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => api.delete(`/icps/${id}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['icps'] });
    },
  });

  const handleCreate = () => {
    setEditingICP(null);
    setIsModalOpen(true);
  };

  const handleEdit = (icp: any) => {
    setEditingICP(icp);
    setIsModalOpen(true);
  };

  if (isLoading) {
    return <div>Carregando ICPs...</div>;
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Perfis de Cliente Ideal</h1>
          <p className="mt-1 text-sm text-gray-600">
            Configure os perfis para qualificar prospects automaticamente
          </p>
        </div>
        <button
          onClick={handleCreate}
          className="btn-primary flex items-center"
        >
          <PlusIcon className="h-5 w-5 mr-2" />
          Novo ICP
        </button>
      </div>

      <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
        {icps?.map((icp: any) => (
          <ICPCard
            key={icp.id}
            icp={icp}
            onEdit={handleEdit}
            onDelete={(id) => deleteMutation.mutate(id)}
          />
        ))}
      </div>

      <ICPModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        icp={editingICP}
        onSubmit={(data) => {
          if (editingICP) {
            updateMutation.mutate({ id: editingICP.id, ...data });
          } else {
            createMutation.mutate(data);
          }
        }}
        isLoading={createMutation.isPending || updateMutation.isPending}
      />
    </div>
  );
}
```

### **DIA 5: COMPANIES + POLIMENTO**

#### **🏢 Companies Page + Detail**

**1. Companies List:**
```tsx
// src/pages/Companies.tsx
import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import CompanyCard from '../components/features/CompanyCard';
import CompanyFilters from '../components/features/CompanyFilters';

export default function Companies() {
  const [filters, setFilters] = useState({
    industry: '',
    minScore: 0,
    location: '',
  });

  const { data: companies, isLoading } = useQuery({
    queryKey: ['companies', filters],
    queryFn: () => {
      const params = new URLSearchParams();
      if (filters.industry) params.append('industry', filters.industry);
      if (filters.minScore > 0) params.append('minScore', filters.minScore.toString());
      if (filters.location) params.append('location', filters.location);
      
      return api.get(`/companies?${params.toString()}`).then(res => res.data);
    },
  });

  if (isLoading) {
    return <div>Carregando empresas...</div>;
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Base de Empresas</h1>
        <p className="mt-1 text-sm text-gray-600">
          {companies?.length || 0} empresas na base de dados
        </p>
      </div>

      <CompanyFilters filters={filters} onFiltersChange={setFilters} />

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {companies?.map((company: any) => (
          <CompanyCard key={company.id} company={company} />
        ))}
      </div>
    </div>
  );
}
```

#### **⚡ Performance + Polish**

**2. Loading States & Error Handling:**
```tsx
// src/components/ui/LoadingSkeleton.tsx
export function SearchLoadingSkeleton() {
  return (
    <div className="bg-white shadow rounded-lg p-6">
      <div className="animate-pulse">
        <div className="h-4 bg-gray-200 rounded w-1/4 mb-4"></div>
        <div className="space-y-3">
          {[...Array(5)].map((_, i) => (
            <div key={i} className="flex space-x-4">
              <div className="h-4 bg-gray-200 rounded w-1/4"></div>
              <div className="h-4 bg-gray-200 rounded w-1/6"></div>
              <div className="h-4 bg-gray-200 rounded w-1/4"></div>
              <div className="h-4 bg-gray-200 rounded w-1/6"></div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

// src/components/ui/ErrorBoundary.tsx
import { Component, ReactNode } from 'react';

interface Props {
  children: ReactNode;
}

interface State {
  hasError: boolean;
}

export class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(): State {
    return { hasError: true };
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="text-center py-12">
          <h2 className="text-xl font-bold text-gray-900">Algo deu errado</h2>
          <p className="mt-2 text-gray-600">Tente recarregar a página</p>
          <button
            onClick={() => window.location.reload()}
            className="mt-4 btn-primary"
          >
            Recarregar
          </button>
        </div>
      );
    }

    return this.props.children;
  }
}
```

**3. Environment Configuration:**
```bash
# .env.local (no frontend)
VITE_API_URL=http://localhost:8080/api
VITE_APP_VERSION=0.1.0
VITE_ENABLE_DEBUG=true
```

---

## ✅ CHECKLIST DE ENTREGA

### **📋 Checklist Dia 1:**
- [x] Projeto React criado e rodando
- [x] TailwindCSS configurado
- [x] React Router funcionando
- [x] Estrutura de pastas organizada
- [x] API service configurado

### **📋 Checklist Dia 2:**
- [x] Layout com header e sidebar
- [x] Dashboard página funcionando
- [x] Métricas básicas exibindo
- [x] Links de navegação funcionando
- [x] Design responsivo básico

### **📋 Checklist Dia 3:**
- [x] Página de busca completa
- [x] Formulário com validação
- [x] Integração com API de search
- [x] Loading states implementados
- [x] Error handling básico

### **📋 Checklist Dia 4:**
- [x] Tabela de resultados funcionando
- [x] ICP management CRUD completo
- [x] Modais e forms funcionando
- [x] React Query cache funcionando
- [x] Filtros básicos implementados

### **📋 Checklist Dia 5:**
- [x] Página de companies funcionando
- [x] Company detail modal/page
- [x] Error boundaries implementados
- [x] Performance otimizada
- [x] Build de produção funcionando

### **📋 Checklist Final (End of Week 2):**
- [x] **Demo Ready:** Interface completa e funcional
- [x] **Performance:** <3s load time, <1s interações
- [x] **Responsive:** Funciona em mobile e desktop
- [x] **Error Handling:** Graceful degradation para API failures
- [x] **Production Build:** `npm run build` sem erros

#### **🔎 Evidências de validação (2026-03-16)**
- Lighthouse (produção local): FCP `261ms`, LCP `1260ms`, TTI `261ms`, TBT `0ms`
- Bundle gzip total: `166,982B` (limite `500KB`)
- Smoke funcional:
  - API: `/api/icps`, `/api/companies`, `/api/leads/search` (status `COMPLETED` com leads)
  - Frontend preview: `/`, `/search`, `/icps`, `/companies` respondendo `200`
- Relatórios locais:
  - `.tmp/lighthouse-week2-prod-provided-backend-on.json`
  - `.tmp/lighthouse-week2-prod-default-backend-on.json`
  - `.tmp/week2-demo-smoke-report.txt`

---

## 🛠️ COMANDOS ÚTEIS

### **Development:**
```bash
# Start frontend dev server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview

# Type checking
npx tsc --noEmit
```

### **Testing Integration:**
```bash
# Ensure backend is running
cd ../
./gradlew bootRun --args="--spring.profiles.active=mock"

# In another terminal, start frontend
cd prospectos-web
npm run dev

# Test full flow:
# 1. Open http://localhost:5173
# 2. Navigate to Search page
# 3. Submit search form
# 4. Verify results appear
```

### **Build Optimization:**
```bash
# Analyze bundle size
npm run build
npx vite-bundle-analyzer dist

# Check for unused dependencies
npx depcheck
```

---

## 🎯 CRITÉRIOS DE SUCESSO

### **✅ Funcional:**
- 4 páginas principais funcionando
- Integração completa com backend
- CRUD de ICPs funcionando
- Search + results funcionando
- Error states implementados

### **📱 UX/UI:**
- Design profissional com TailwindCSS
- Responsive (mobile + desktop)
- Loading states informativos
- Error messages úteis
- Navegação intuitiva

### **⚡ Performance:**
- Bundle size <500KB (gzipped)
- First paint <2s
- Interações <1s response time
- React Query cache otimizado

---

## 🚀 PREPARAÇÃO PARA SEMANA 3

### **Environment para Python:**
```bash
# Python setup (verificar durante Semana 2)
python --version  # Should be 3.9+
pip --version

# Prepare workspace
mkdir scraper-service
cd scraper-service

# Next week: Flask + scraping implementation
```

### **Frontend Enhancements Queue:**
- [x] Export CSV functionality
- [x] Advanced filters
- [x] Pagination for large result sets
- [x] Company detail page
- [x] ICP matching visualization

---

**🎉 Ao final da Semana 2, você terá uma interface web profissional que torna o produto usável por qualquer usuário, não apenas desenvolvedores!**

*"Backend funcional + Frontend bonito = Produto vendável"*
