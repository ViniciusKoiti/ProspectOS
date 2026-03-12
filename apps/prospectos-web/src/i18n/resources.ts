export const resources = {
    'pt-BR': {
        translation: {
            common: {
                appName: 'ProspectOS Web',
                week2: 'semana 2',
                language: 'Idioma',
                loading: 'Carregando...',
                retry: 'Tentar novamente',
                refresh: 'Atualizar',
                newIcp: 'Novo ICP',
                searchProspects: 'Buscar prospects',
                backToCompanies: 'Voltar para empresas',
                cancel: 'Cancelar',
                save: 'Salvar',
                create: 'Criar',
                edit: 'Editar',
                delete: 'Excluir',
                actions: 'Acoes',
                close: 'Fechar',
                languageNames: {
                    'pt-BR': 'Portugues (BR)',
                    en: 'English',
                    es: 'Espanol',
                },
            },
            nav: {
                dashboard: 'Dashboard',
                search: 'Busca',
                icps: 'ICPs',
                companies: 'Empresas',
            },
            pages: {
                dashboard: {
                    title: 'Dashboard',
                    description: 'Visao geral do progresso do frontend na Semana 2.',
                    stats: {
                        companies: 'Empresas',
                        activeIcps: 'ICPs ativos',
                        searchesToday: 'Buscas hoje',
                        trend: 'fase de base',
                    },
                    errors: {
                        load: 'Falha ao carregar os indicadores do dashboard.',
                    },
                },
                search: {
                    title: 'Busca',
                    description: 'Use componentes reutilizaveis para manter consistencia entre os fluxos.',
                    fields: {
                        query: 'Consulta',
                        icp: 'ICP',
                        limit: 'Limite de resultados',
                    },
                    placeholders: {
                        query: 'CTOs de fintech em Sao Paulo',
                        selectIcp: 'Selecione um ICP',
                    },
                    options: {
                        techLeaders: 'Liderancas de tecnologia',
                        b2bOps: 'Operacoes B2B SaaS',
                    },
                    empty: {
                        title: 'Nenhuma busca executada',
                        description: 'Os resultados aparecerao aqui apos o envio do formulario.',
                    },
                    errors: {
                        loadIcps: 'Falha ao carregar as opcoes de ICP.',
                        execute: 'Falha ao executar a busca.',
                    },
                },
                icps: {
                    title: 'ICPs',
                    description: 'Padroes de tabela e acao compartilhados para consistencia no CRUD.',
                    table: {
                        name: 'Nome',
                        focus: 'Foco',
                        regions: 'Regioes',
                    },
                    form: {
                        name: 'Nome',
                        description: 'Descricao',
                        industries: 'Industrias',
                        regions: 'Regioes',
                        targetRoles: 'Cargos alvo',
                        interestTheme: 'Tema de interesse',
                        technologies: 'Tecnologias alvo',
                        minEmployees: 'Minimo de funcionarios',
                        maxEmployees: 'Maximo de funcionarios',
                        multiValueHint: 'Use virgula, ponto e virgula ou quebra de linha para separar itens.',
                    },
                    actions: {
                        edit: 'Editar ICP',
                        delete: 'Excluir ICP',
                    },
                    modal: {
                        createTitle: 'Criar ICP',
                        createDescription: 'Defina o perfil ideal do cliente com campos reutilizaveis e tipados.',
                        editTitle: 'Editar ICP',
                        editDescription: 'Atualize o ICP mantendo o mesmo contrato usado na criacao.',
                    },
                    confirmDelete: {
                        title: 'Excluir ICP',
                        description: 'Deseja realmente excluir "{{name}}"? Esta acao nao pode ser desfeita.',
                    },
                    feedback: {
                        createSuccess: 'ICP criado com sucesso.',
                        updateSuccess: 'ICP atualizado com sucesso.',
                        deleteSuccess: 'ICP excluido com sucesso.',
                    },
                    errors: {
                        load: 'Falha ao carregar ICPs.',
                        save: 'Falha ao salvar o ICP.',
                        delete: 'Falha ao excluir o ICP.',
                    },
                    empty: {
                        title: 'Nenhum ICP configurado',
                        description: 'Crie o primeiro ICP para habilitar descoberta direcionada.',
                    },
                },
                companies: {
                    title: 'Empresas',
                    description: 'Primitivos consistentes de tabela facilitam evolucao das listas.',
                    table: {
                        company: 'Empresa',
                        industry: 'Industria',
                        score: 'Score',
                    },
                    empty: {
                        title: 'Nenhuma empresa disponivel',
                        description: 'Leads aceitos aparecerao aqui apos integracao dos fluxos.',
                    },
                    errors: {
                        load: 'Falha ao carregar empresas.',
                    },
                },
                companyDetail: {
                    title: 'Detalhe da empresa',
                    selectedId: 'ID selecionado: {{id}}',
                    profileTitle: 'Resumo do perfil',
                    scoreUnavailable: 'Sem score',
                    description: 'Esta tela recebera dados reais apos finalizacao do contrato de detalhes.',
                    errors: {
                        invalidId: 'ID de empresa invalido.',
                        load: 'Falha ao carregar o detalhe da empresa.',
                    },
                },
            },
            ui: {
                table: {
                    noRecords: 'Sem registros',
                    noData: 'Nao ha dados disponiveis para esta visualizacao.',
                },
                error: {
                    title: 'Algo deu errado',
                },
                loading: {
                    label: 'Carregando dados...',
                },
                modal: {
                    close: 'Fechar modal',
                },
            },
        },
    },
    en: {
        translation: {
            common: {
                appName: 'ProspectOS Web',
                week2: 'week 2',
                language: 'Language',
                loading: 'Loading...',
                retry: 'Retry',
                refresh: 'Refresh',
                newIcp: 'New ICP',
                searchProspects: 'Search prospects',
                backToCompanies: 'Back to companies',
                cancel: 'Cancel',
                save: 'Save',
                create: 'Create',
                edit: 'Edit',
                delete: 'Delete',
                actions: 'Actions',
                close: 'Close',
                languageNames: {
                    'pt-BR': 'Portuguese (BR)',
                    en: 'English',
                    es: 'Spanish',
                },
            },
            nav: {
                dashboard: 'Dashboard',
                search: 'Search',
                icps: 'ICPs',
                companies: 'Companies',
            },
            pages: {
                dashboard: {
                    title: 'Dashboard',
                    description: 'Overview of Week 2 frontend progress.',
                    stats: {
                        companies: 'Companies',
                        activeIcps: 'Active ICPs',
                        searchesToday: 'Searches today',
                        trend: 'bootstrap phase',
                    },
                    errors: {
                        load: 'Failed to load dashboard metrics.',
                    },
                },
                search: {
                    title: 'Search',
                    description: 'Use reusable components to keep flows consistent across features.',
                    fields: {
                        query: 'Query',
                        icp: 'ICP',
                        limit: 'Result limit',
                    },
                    placeholders: {
                        query: 'CTOs of fintech companies in Sao Paulo',
                        selectIcp: 'Select an ICP',
                    },
                    options: {
                        techLeaders: 'Technology leaders',
                        b2bOps: 'B2B SaaS operations',
                    },
                    empty: {
                        title: 'No search executed yet',
                        description: 'Results will appear here after form submission.',
                    },
                    errors: {
                        loadIcps: 'Failed to load ICP options.',
                        execute: 'Failed to execute search.',
                    },
                },
                icps: {
                    title: 'ICPs',
                    description: 'Shared table and action patterns keep CRUD screens consistent.',
                    table: {
                        name: 'Name',
                        focus: 'Focus',
                        regions: 'Regions',
                    },
                    form: {
                        name: 'Name',
                        description: 'Description',
                        industries: 'Industries',
                        regions: 'Regions',
                        targetRoles: 'Target roles',
                        interestTheme: 'Interest theme',
                        technologies: 'Target technologies',
                        minEmployees: 'Minimum employees',
                        maxEmployees: 'Maximum employees',
                        multiValueHint: 'Use commas, semicolons, or line breaks to separate items.',
                    },
                    actions: {
                        edit: 'Edit ICP',
                        delete: 'Delete ICP',
                    },
                    modal: {
                        createTitle: 'Create ICP',
                        createDescription: 'Define the ideal customer profile using typed reusable fields.',
                        editTitle: 'Edit ICP',
                        editDescription: 'Update the ICP using the same contract as creation.',
                    },
                    confirmDelete: {
                        title: 'Delete ICP',
                        description: 'Do you really want to delete "{{name}}"? This action cannot be undone.',
                    },
                    feedback: {
                        createSuccess: 'ICP created successfully.',
                        updateSuccess: 'ICP updated successfully.',
                        deleteSuccess: 'ICP deleted successfully.',
                    },
                    errors: {
                        load: 'Failed to load ICPs.',
                        save: 'Failed to save the ICP.',
                        delete: 'Failed to delete the ICP.',
                    },
                    empty: {
                        title: 'No ICPs configured',
                        description: 'Create your first ICP to enable targeted lead discovery.',
                    },
                },
                companies: {
                    title: 'Companies',
                    description: 'Consistent table primitives make list screens easier to extend.',
                    table: {
                        company: 'Company',
                        industry: 'Industry',
                        score: 'Score',
                    },
                    empty: {
                        title: 'No companies available',
                        description: 'Accepted leads will be listed here once flows are integrated.',
                    },
                    errors: {
                        load: 'Failed to load companies.',
                    },
                },
                companyDetail: {
                    title: 'Company detail',
                    selectedId: 'Selected id: {{id}}',
                    profileTitle: 'Profile snapshot',
                    scoreUnavailable: 'No score',
                    description: 'This screen will receive backend data after details contract finalization.',
                    errors: {
                        invalidId: 'Invalid company id.',
                        load: 'Failed to load company detail.',
                    },
                },
            },
            ui: {
                table: {
                    noRecords: 'No records',
                    noData: 'No data available for this view.',
                },
                error: {
                    title: 'Something went wrong',
                },
                loading: {
                    label: 'Loading data...',
                },
                modal: {
                    close: 'Close modal',
                },
            },
        },
    },
    es: {
        translation: {
            common: {
                appName: 'ProspectOS Web',
                week2: 'semana 2',
                language: 'Idioma',
                loading: 'Cargando...',
                retry: 'Reintentar',
                refresh: 'Actualizar',
                newIcp: 'Nuevo ICP',
                searchProspects: 'Buscar prospectos',
                backToCompanies: 'Volver a empresas',
                cancel: 'Cancelar',
                save: 'Guardar',
                create: 'Crear',
                edit: 'Editar',
                delete: 'Eliminar',
                actions: 'Acciones',
                close: 'Cerrar',
                languageNames: {
                    'pt-BR': 'Portugues (BR)',
                    en: 'Ingles',
                    es: 'Espanol',
                },
            },
            nav: {
                dashboard: 'Dashboard',
                search: 'Busqueda',
                icps: 'ICPs',
                companies: 'Empresas',
            },
            pages: {
                dashboard: {
                    title: 'Dashboard',
                    description: 'Vista general del progreso del frontend en la Semana 2.',
                    stats: {
                        companies: 'Empresas',
                        activeIcps: 'ICPs activos',
                        searchesToday: 'Busquedas hoy',
                        trend: 'fase base',
                    },
                    errors: {
                        load: 'No se pudieron cargar los indicadores del dashboard.',
                    },
                },
                search: {
                    title: 'Busqueda',
                    description: 'Usa componentes reutilizables para mantener consistencia entre flujos.',
                    fields: {
                        query: 'Consulta',
                        icp: 'ICP',
                        limit: 'Limite de resultados',
                    },
                    placeholders: {
                        query: 'CTOs de fintech en Sao Paulo',
                        selectIcp: 'Selecciona un ICP',
                    },
                    options: {
                        techLeaders: 'Liderazgo tecnologico',
                        b2bOps: 'Operaciones B2B SaaS',
                    },
                    empty: {
                        title: 'Aun no se ejecuto ninguna busqueda',
                        description: 'Los resultados apareceran aqui despues del envio del formulario.',
                    },
                    errors: {
                        loadIcps: 'No se pudieron cargar las opciones de ICP.',
                        execute: 'No se pudo ejecutar la busqueda.',
                    },
                },
                icps: {
                    title: 'ICPs',
                    description: 'Patrones compartidos de tabla y acciones para consistencia en CRUD.',
                    table: {
                        name: 'Nombre',
                        focus: 'Enfoque',
                        regions: 'Regiones',
                    },
                    form: {
                        name: 'Nombre',
                        description: 'Descripcion',
                        industries: 'Industrias',
                        regions: 'Regiones',
                        targetRoles: 'Cargos objetivo',
                        interestTheme: 'Tema de interes',
                        technologies: 'Tecnologias objetivo',
                        minEmployees: 'Minimo de empleados',
                        maxEmployees: 'Maximo de empleados',
                        multiValueHint: 'Usa comas, punto y coma o saltos de linea para separar elementos.',
                    },
                    actions: {
                        edit: 'Editar ICP',
                        delete: 'Eliminar ICP',
                    },
                    modal: {
                        createTitle: 'Crear ICP',
                        createDescription: 'Define el perfil ideal del cliente usando campos reutilizables y tipados.',
                        editTitle: 'Editar ICP',
                        editDescription: 'Actualiza el ICP usando el mismo contrato que en la creacion.',
                    },
                    confirmDelete: {
                        title: 'Eliminar ICP',
                        description: 'Deseas eliminar "{{name}}"? Esta accion no se puede deshacer.',
                    },
                    feedback: {
                        createSuccess: 'ICP creado correctamente.',
                        updateSuccess: 'ICP actualizado correctamente.',
                        deleteSuccess: 'ICP eliminado correctamente.',
                    },
                    errors: {
                        load: 'No se pudieron cargar los ICPs.',
                        save: 'No se pudo guardar el ICP.',
                        delete: 'No se pudo eliminar el ICP.',
                    },
                    empty: {
                        title: 'No hay ICPs configurados',
                        description: 'Crea el primer ICP para habilitar descubrimiento dirigido.',
                    },
                },
                companies: {
                    title: 'Empresas',
                    description: 'Primitivos consistentes de tabla facilitan la evolucion de listados.',
                    table: {
                        company: 'Empresa',
                        industry: 'Industria',
                        score: 'Puntaje',
                    },
                    empty: {
                        title: 'No hay empresas disponibles',
                        description: 'Los leads aceptados apareceran aqui al integrar los flujos.',
                    },
                    errors: {
                        load: 'No se pudieron cargar las empresas.',
                    },
                },
                companyDetail: {
                    title: 'Detalle de empresa',
                    selectedId: 'ID seleccionado: {{id}}',
                    profileTitle: 'Resumen del perfil',
                    scoreUnavailable: 'Sin puntaje',
                    description: 'Esta pantalla recibira datos reales cuando se finalice el contrato de detalle.',
                    errors: {
                        invalidId: 'ID de empresa invalido.',
                        load: 'No se pudo cargar el detalle de la empresa.',
                    },
                },
            },
            ui: {
                table: {
                    noRecords: 'Sin registros',
                    noData: 'No hay datos disponibles para esta vista.',
                },
                error: {
                    title: 'Algo salio mal',
                },
                loading: {
                    label: 'Cargando datos...',
                },
                modal: {
                    close: 'Cerrar modal',
                },
            },
        },
    },
} as const;
