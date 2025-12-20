# Documentação de Internacionalização - ProspectOS

Esta pasta contém a documentação completa para converter todos os elementos em português do código para inglês, organizados em atividades específicas.

## 📋 Índice de Atividades

| Atividade | Arquivo | Descrição | Prioridade |
|-----------|---------|-----------|------------|
| 01 | [system-prompts.md](./01-system-prompts.md) | Converter system prompts dos LLMs para inglês | 🔴 Alta |
| 02 | [log-messages.md](./02-log-messages.md) | Padronizar mensagens de log em inglês | 🟡 Média |
| 03 | [ai-templates.md](./03-ai-templates.md) | Converter templates de prompts de IA | 🔴 Alta |
| 04 | [enum-constants.md](./04-enum-constants.md) | Traduzir enums e constantes | 🟢 Baixa |
| 05 | [mock-responses.md](./05-mock-responses.md) | Converter implementações mock para inglês | 🟡 Média |
| 06 | [documentation.md](./06-documentation.md) | Traduzir comentários JavaDoc e documentação | 🟢 Baixa |
| 07 | [json-fields.md](./07-json-fields.md) | Padronizar campos JSON em inglês | 🟡 Média |
| 08 | [i18n-setup.md](./08-i18n-setup.md) | Configurar Spring i18n para mensagens de usuário | 🟡 Média |

## 🎯 Ordem de Execução Recomendada

### Fase 1: Core AI (Crítico para qualidade dos LLMs)
1. **System Prompts** (01) - Impacto direto na qualidade das respostas de IA
2. **AI Templates** (03) - Prompts específicos dos serviços de IA

### Fase 2: Logging e Debugging
3. **Log Messages** (02) - Padronização para ambiente internacional

### Fase 3: Estruturas de Dados
4. **JSON Fields** (07) - APIs e interfaces
5. **Mock Responses** (05) - Testes e desenvolvimento

### Fase 4: Melhorias Gerais
6. **Enum Constants** (04) - Constantes do sistema
7. **Documentation** (06) - Comentários e JavaDoc
8. **i18n Setup** (08) - Infraestrutura para futuras mensagens de usuário

## ⚡ Quick Start

Para começar rapidamente:

```bash
# 1. Execute as atividades críticas primeiro
# Siga: docs/01-system-prompts.md
# Depois: docs/03-ai-templates.md

# 2. Teste a qualidade das respostas de IA
# Execute os exemplos em AIUsageExample.java

# 3. Continue com as demais atividades conforme prioridade
```

## 🔧 Ferramentas Recomendadas

- **IDE**: IntelliJ IDEA ou VS Code com extensões Java
- **Busca**: Grep/ripgrep para encontrar strings em português
- **Validação**: Executar testes após cada mudança
- **LLM Testing**: Testar qualidade das respostas após converter prompts

## 📊 Impacto Esperado

| Área | Antes | Depois |
|------|-------|--------|
| **Qualidade LLM** | Mista (PT/EN) | ⭐⭐⭐⭐⭐ Alta (EN) |
| **Logs** | Português | 🌍 Internacional |
| **APIs** | Misto | 📋 Padronizado |
| **Documentação** | Português | 📖 Inglês |
| **Manutenção** | Localizada | 🔧 Global |

## 🚨 Avisos Importantes

⚠️ **Backup**: Faça backup antes de iniciar as alterações
⚠️ **Testes**: Execute testes após cada atividade  
⚠️ **LLM Quality**: Monitore a qualidade das respostas de IA após converter prompts
⚠️ **Gradual**: Implemente as mudanças de forma incremental

---

*Última atualização: $(date)*