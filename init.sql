
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DO $$
BEGIN
    RAISE NOTICE 'Banco prospectos inicializado com sucesso!';
    RAISE NOTICE 'Usando InMemoryVectorIndex para busca semântica';
END $$;
