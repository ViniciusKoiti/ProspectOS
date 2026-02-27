# MVP-008: Doc-Drift Control - Documentation Consistency Governance

**Tipo**: Governança  
**Módulo**: All  
**Prioridade**: P2 - Média  
**Situação**: Planejada

## Objetivo

Implementar controles sistemáticos para prevenir divergências recorrentes entre documentação e código, garantindo que a documentação permaneça sincronizada com o estado real do projeto.

## Contexto

Durante a análise WS4 (workspace 4 - documentação e backlog), foram identificadas várias divergências críticas entre documentação e código:
- Chaves Groq inconsistentes entre código e docs
- Valores de `allowed-sources` diferentes entre docs técnicos e arquivos de propriedades  
- Configurações de perfis desatualizadas

## Escopo

### 1. Checklist Obrigatório para PRs de Configuração

Criar template de PR com checklist para mudanças que envolvam:
- Profiles (`application-*.properties`)
- Variáveis de ambiente (`.env.example`, `DotenvEnvironmentPostProcessor`)
- Propriedades default
- Comportamento de teste
- Configurações de AI providers

### 2. Documentação de Referência

Estabelecer docs de referência que devem ser mantidos sincronizados:
- `CLAUDE.md` - Comandos e arquitetura
- `README.md` - Configuração e uso
- `.env.example` - Variáveis de ambiente
- `docs/technical-debt/README.md` - Estado do débito técnico

### 3. Validação Automatizada (Futuro)

Implementar verificações automatizadas:
- Script que compara valores entre `application-*.properties`
- Validação de que chaves mencionadas em docs existem no código
- Verificação de que exemplos de `.env` são válidos

## Deliverables

### Imediato (Fase 3A)
- [ ] Criar `.github/pull_request_template.md` com checklist de configuração
- [ ] Documentar "fontes da verdade" para configurações no `CLAUDE.md`
- [ ] Atualizar `docs/tasks/index.md` para incluir esta task

### Médio Prazo (Fase 3B) 
- [ ] Script de validação de consistência de configs
- [ ] Integração com CI para verificar divergências
- [ ] Docs sobre processo de manutenção de docs

## Critérios de Aceite

### Para Fase 3A
- [ ] PR template existe e contém checklist específico para mudanças de config
- [ ] CLAUDE.md documenta claramente quais arquivos são "fonte da verdade"
- [ ] Não existem divergências conhecidas entre docs e código
- [ ] Task registrada no índice de tasks

### Para Fase 3B
- [ ] Script de validação funciona e detecta inconsistências
- [ ] CI executa verificações automaticamente
- [ ] Documentação do processo está atualizada

## Arquivos Afetados

### Templates e Automação
- `.github/pull_request_template.md` (novo)
- `scripts/validate-config-consistency.sh` (futuro)
- `.github/workflows/doc-validation.yml` (futuro)

### Documentação
- `docs/tasks/index.md` (atualizar)
- `CLAUDE.md` (atualizar seção de workflow)
- `docs/technical-debt/README.md` (manter atualizado)

### Configuração
- `src/main/resources/application*.properties` (manter sincronizados)
- `.env.example` (manter como referência)
- `src/main/java/dev/prospectos/config/DotenvEnvironmentPostProcessor.java`

## Estratégia de Implementação

1. **Identificar "Fontes da Verdade"**: Para cada tipo de configuração, definir qual arquivo é autoritativo
2. **Criar Processo Manual**: Checklist e processo para desenvolvedores
3. **Automatizar Gradualmente**: Começar com validações simples e evoluir
4. **Cultura de Manutenção**: Estabelecer responsabilidade por docs atualizadas

## Benefícios Esperados

- **Redução de tempo de onboarding**: Docs sempre corretos
- **Menos bugs de configuração**: Consistência entre ambientes
- **Maior confiabilidade**: Troubleshooting com informações corretas
- **Sustentabilidade**: Processo escalável conforme projeto cresce

## Riscos e Mitigação

**Risco**: Processo pode ser ignorado pelos desenvolvedores  
**Mitigação**: Integrar no CI e fazer parte do processo de review

**Risco**: Validações podem ser muito rígidas e bloquear desenvolvimento  
**Mitigação**: Começar com validações simples e evoluir baseado em feedback

## Relacionado

- **WS4**: `docs/workspaces/p0/ws4-documentacao-backlog/START.md`
- **TD-003**: Correção de dependências de .env em testes
- **CLAUDE.md**: Documentação principal para Claude Code
- **README.md**: Documentação principal para usuários

---

**Criado em**: 2026-02-27  
**Última atualização**: 2026-02-27  
**Responsável**: Arquitetura / DevOps