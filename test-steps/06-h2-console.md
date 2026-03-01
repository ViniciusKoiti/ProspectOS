# PASSO 6: Validação do H2 Console

## Objetivo  
Verificar se o banco H2 está funcionando e os dados estão corretos.

## Comandos para Executar

### 6.1 Acessar H2 Console
```
Abrir no navegador: http://localhost:8080/h2-console

Configurações de conexão:
- Driver Class: org.h2.Driver  
- JDBC URL: jdbc:h2:mem:prospectos
- User Name: sa
- Password: (deixar vazio)
```

### 6.2 Verificar Tabelas
```sql
-- Ver todas as tabelas
SHOW TABLES;
```
**Esperado:** Tabelas como COMPANY, ICP, COMPANY_CONTACTS, etc.

### 6.3 Contar Registros
```sql
-- Contar empresas
SELECT COUNT(*) FROM COMPANY;

-- Contar ICPs
SELECT COUNT(*) FROM ICP;

-- Ver primeiras empresas
SELECT * FROM COMPANY LIMIT 5;
```

### 6.4 Verificar Dados Brasileiros
```sql
-- Empresas do Brasil
SELECT name, industry, location 
FROM COMPANY 
WHERE location LIKE '%Brazil%' 
LIMIT 10;

-- Empresas de tecnologia
SELECT name, industry, description 
FROM COMPANY 
WHERE industry IN ('fintech', 'technology', 'saas') 
LIMIT 10;
```

### 6.5 Verificar ICPs Criados
```sql
-- Ver todos os ICPs
SELECT name, description, target_roles, regions 
FROM ICP;
```

## Critérios de Sucesso
- [ ] H2 Console acessível via navegador
- [ ] Conexão com database funciona
- [ ] Tabelas foram criadas automaticamente
- [ ] 50+ empresas na tabela COMPANY
- [ ] 3 ICPs na tabela ICP
- [ ] Empresas brasileiras estão presentes
- [ ] Dados estão bem estruturados

## Dados Esperados

### Empresas Brasileiras Esperadas:
```sql
SELECT name, industry, location 
FROM COMPANY 
WHERE name IN ('Nubank', 'Stone Pagamentos', 'SLC Agrícola', 'Magazine Luiza')
ORDER BY name;
```

### ICPs Esperados:
- **CTOs de Startups Tech**
- **Diretores do Agronegócio**  
- **Founders de Fintech**

## Validações Avançadas
```sql
-- Verificar distribuição por indústria
SELECT industry, COUNT(*) as total 
FROM COMPANY 
GROUP BY industry 
ORDER BY total DESC;

-- Verificar empresas por localização
SELECT location, COUNT(*) as total 
FROM COMPANY 
WHERE location LIKE '%Brazil%'
GROUP BY location 
ORDER BY total DESC;
```

## Em Caso de Erro
- Verificar URL do H2 Console
- Confirmar configurações de conexão
- Verificar se aplicação ainda está rodando
- Verificar logs de DataSeeder

## Próximo Passo
Se H2 Console funciona → **PASSO 7: Testes de Performance**