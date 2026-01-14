---
id: TD-002
title: "Arquivo .env commitado no repositório com configurações sensíveis"
status: "open"
severity: "critical"
type: "security"
module: "cross"
introduced_at: "2024/2025 - configuração inicial de desenvolvimento"
tags:
  - "security"
  - "secrets"
  - "git"
  - "environment"
---

# Contexto
O projeto ProspectOS utiliza arquitetura modular com Spring Modulith e integra com serviços externos (OpenAI, Anthropic, News API) que requerem API keys. O DotenvEnvironmentPostProcessor processa variáveis do arquivo .env, mas este arquivo foi commitado no repositório.

# Evidências (com caminhos)
- Arquivo: `.env` (todo o arquivo está no repositório)
- Configurações sensíveis presentes:
  ```env
  SPRING_DATASOURCE_PASSWORD=password  # linha 32
  NEWS_API_KEY=test-key                 # linha 49
  JWT_SECRET=dev-secret-key-change-in-production  # linha 57
  OPENAI_API_KEY =                      # linha 109 (vazio mas expõe estrutura)
  ```
- Arquivo: `.gitignore` contém proteções, mas .env já foi commitado:
  ```gitignore
  ### AI API Keys ###
  *api-key*
  *secret*
  *token*
  ```

# Por que isso é um débito técnico
Commit de arquivos .env representa grave violação de segurança:
- **Exposição de credenciais**: Senhas e secrets ficam visíveis no histórico Git
- **Risco de vazamento**: Qualquer pessoa com acesso ao repo vê as configurações
- **Histórico persistente**: Mesmo removendo o arquivo, credenciais ficam no histórico Git
- **Padrão ruim**: Normaliza commit de informações sensíveis
- **Compliance**: Viola práticas de segurança e pode impactar auditorias

# Impacto
- **Impacto técnico**: Credenciais expostas permanentemente no Git history, risco de acesso não autorizado
- **Impacto no produto**: Potencial comprometimento de APIs externas, vazamento de dados, violações de compliance
- **Probabilidade**: Muito alta (arquivo já está commitado)
- **Urgência**: Crítica (risco de segurança ativo)

# Estratégias de correção
1. **Opção A (rápida)**: Remover .env e reescrever histórico
   - `git rm .env`
   - `git filter-branch --index-filter 'git rm --cached --ignore-unmatch .env'`
   - Atualizar .gitignore com regra específica `.env`
   - Regenerar todas as credenciais expostas
   - Esforço: S (2-3 horas)
   - Prós: Remove completamente o risco
   - Contras: Requer regeneração de credenciais

2. **Opção B (ideal)**: Implementar gestão de secrets robusta
   - Usar .env.example para template
   - Implementar validação de startup para secrets obrigatórios
   - Configurar CI/CD com secrets management
   - Documentar processo no CLAUDE.md
   - Esforço: M (6-8 horas)
   - Prós: Solução completa e sustentável
   - Contras: Mais complexo, requer setup de infra

# Critério de pronto (DoD)
- [ ] Arquivo .env removido do repositório e histórico Git
- [ ] Todas as credenciais expostas foram regeneradas
- [ ] .gitignore atualizado com regra específica para .env
- [ ] .env.example documentado com todas as variáveis necessárias
- [ ] Processo de configuração documentado no CLAUDE.md
- [ ] Validação de startup implementada para secrets críticos
- [ ] Testes passam com configuração apenas via .env.example

# Observações
O arquivo contém principalmente valores de desenvolvimento ("password", "test-key"), mas o padrão é problemático. JWT_SECRET em especial deve ser gerado aleatoriamente. Considerar uso de ferramentas como git-secrets ou pre-commit hooks para prevenir futuros commits de secrets.